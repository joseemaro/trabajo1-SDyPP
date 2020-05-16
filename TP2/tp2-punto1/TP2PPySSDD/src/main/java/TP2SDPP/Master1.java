package TP2SDPP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

//Encargado de registrar sus configuraciones en un archivo (Config.java), tenemos una funcion para realizar ping a todos los Maestros que conoce y espera las conexiones nuevas.

public class Master1 {
	String ip;
	int port;
	ArrayList<String[]> otherMasters;
	ArrayList<String[]> connMasters;
	ArrayList<MaestroIndex> peerData;
	Map<String, String> mapPeer;
	private final Logger log = LoggerFactory.getLogger(Master1.class);

	@SuppressWarnings("resource")
	public Master1 (String ip,int port) {
		this.port = port;
		this.ip = ip;
		this.otherMasters = new ArrayList<String[]>();
		this.connMasters = new ArrayList<String[]>();
		this.peerData = new ArrayList<MaestroIndex>();
		this.mapPeer = new HashMap<String,String>();
		MDC.put("log.name", Master1.class.getSimpleName().toString()+"-"+this.port);
		registrarConfig(this.ip,this.port);
		
		try {
			ServerSocket ss = new ServerSocket (this.port);
			boolean esta = false;
			log.info("[Servidor Maestro-"+ port +"]: Listo para servir");
			for(String s[] : this.otherMasters) {
				if(s.length>0) {
					esta = available(s[0],Integer.parseInt(s[1]));
				}
				if (esta) {
					log.info("[SERVER MASTER-"+port+"]: El servidor "+s[0]+":"+s[1]+" esta ON. Registrandolo.");
					this.connMasters.add(s);
					esta = false;
				}else {
					log.info("[SERVER MASTER-"+port+"]: El servidor "+s[0]+":"+s[1]+" esta OFF.");
				}
			}
			int counter = 0;
			while (true) {
				Socket client = ss.accept();
				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
				String msg = inputChannel.readLine();
				if(msg.contains("imMaster")) {
					String[] strPort = msg.split(":");
					log.info("[SERVER MASTER-"+port+"]: Me pingueo "+client.getInetAddress().getHostAddress()+" Corriendo en "+strPort[1]);
					log.info("[SERVER MASTER-"+port+"]: Registrando en Masters conectados");
					String newIp = client.getInetAddress().getHostAddress();
					String newPort = strPort[1];
					String [] newServer = {newIp,newPort};
					if(!newIp.isEmpty() && !newPort.isEmpty()) {
						newServer[0] = newIp;
						newServer[1] = newPort;
						this.connMasters.add(newServer);
					}
					log.info("[SERVER MASTER-"+port+"]: Recibiendo sus archivos...");
					receiveMasterIndex(client);
					Thread.sleep(200);
					sendMasterIndex(client);
					client.close();
				}
				if(msg.contains("imClientPinging")){
					client.close();
				}
				if(msg.contains("actualizarWorker")){
					sendMasterIndex(client);
					client.close();
				}
				if(msg.contains("bajaPeer")){
					erasePeer(client);
					client.close();
				}
				if(msg.contains("actualizarServerMaster")) {
					String [] m = msg.split("=");
					m = m[1].split(":");
					actualizarByObj(m[0], m[1], client);
					client.close();
				}
				//Mensaje para añadir,actualizar peers  +  lanzar un master para atender sus peticiones
				if(msg.contains("peerServerPortOn=")) {
					String[] strParced = msg.split("=");
					counter++;
					log.info("[SERVER MASTER-"+port+"]: Conexion con el cliente nro: "+counter);
					log.info("[SERVER MASTER-"+port+"]: Info Cliente: "+client.getInetAddress()+":"+client.getPort());
					//Pedir informacion y registrarlo.
					requestClientData(client,strParced[1]);
					// THREAD
					Maestro m = new Maestro (ip,this.port,client,this.peerData,this.connMasters);
					Thread tMaster = new Thread (m);
					tMaster.start();	
				}
			}
		} catch (IOException | InterruptedException e) {
			log.info("[SERVER MASTER-"+port+"]: Socket on port "+port+" is used ");
		}
	}

	private void erasePeer(Socket client) {
		String toErase;
		Map<String, String> aux = new HashMap<String, String>(this.mapPeer);
		try {
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
			toErase = inputChannel.readLine();
			if(toErase != null) {
				while(eraseByValue(aux, toErase) != null)
				this.mapPeer = new HashMap<>(aux);
				this.hashToMasterIndex();
				//Debería de avisarle a otros masters
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMasterIndex(Socket client) {
		try {
			ArrayList<MaestroIndex> toSend;
			ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
			toSend = this.peerData;
			os.writeObject(toSend);
		} catch (IOException e) {
			e.printStackTrace();
		}	     
	}
	@SuppressWarnings("unchecked")
	private void receiveMasterIndex(Socket client) {
		try {
			ArrayList<MaestroIndex> returnMessage = null;
			ObjectInputStream is = new ObjectInputStream(client.getInputStream());
			returnMessage = (ArrayList<MaestroIndex>) is.readObject();
			updByMasterIndex(returnMessage);						
		} catch (Exception e) {
			
		}
	}

	public void registrarConfig(String ip, int port) {
		Configuraciones c = new Configuraciones("./config.txt");
		this.otherMasters = c.doConfig(ip, port);
	}
	
	public void addPeer(String peer, ArrayList<Archivo> peerSharedData) {
		synchronized (this.peerData) {
			boolean esta = false;
			for(MaestroIndex m : this.peerData) {
				if(m.getOwner().contentEquals(peer)) {
					log.info("[SERVER MASTER-"+port+"]:Ya tenia agregado el peer. Agregando la lista.");
					m.setLiArchivo(peerSharedData);
					esta = true;
				}
			}
			if(!esta)
				this.peerData.add(new MaestroIndex(peer,peerSharedData));
		}
	}
	
	private boolean available(String ip,int port) {
	    try (Socket ignored = new Socket(ip, port)) {
			PrintWriter outputChannel = new PrintWriter (ignored.getOutputStream(), true);
			outputChannel.println("imMaster:"+this.port);
			Thread.sleep(200);
			sendMasterIndex(ignored);
			Thread.sleep(200);
			receiveMasterIndex(ignored);
			
			ignored.close();
	        return true;
	    } catch (IOException | InterruptedException ignored) {
	        return false;
	    }
	}

	private void requestClientData(Socket client, String peerPort) {
		try {				
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
			ArrayList<Archivo> store = new ArrayList<Archivo>();
			String msg;
			boolean salir = false;
			String data = client.getInetAddress().getHostName()+":"+peerPort;
			while(!salir){
				if((msg = inputChannel.readLine()) != null) {
					if(!msg.contentEquals(".END")) {
						this.mapPeer.put(msg,data);
						store.add(new Archivo(msg));
					}else {
						salir = true;
						addPeer(data,store);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void actualizarByObj(String ip, String port, Socket master) {
		try {
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (master.getInputStream()));
			log.info("[SERVER MASTER-"+this.port+"]: actualizarByObj() from "+master.getInetAddress().getHostName()+":"+master.getPort());
			String msgFromServer;
			boolean salir = false;
			while(!salir) {
				msgFromServer = inputChannel.readLine();
				if(msgFromServer.contains("sending")){
					try {
						synchronized (this.peerData) {
							ArrayList<MaestroIndex> returnMessage = null;
							ObjectInputStream is = new ObjectInputStream(master.getInputStream());
							returnMessage = (ArrayList<MaestroIndex>) is.readObject();
							log.info("[SERVER MASTER-"+this.port+"]: Recibiendo contenido actualizado de"+master.getInetAddress().getHostAddress()+":"+master.getPort());
							//Hasheo por nombre de archivo + me actualizo mi lista a partir del hash 
							updByMasterIndex(returnMessage);							
							salir = true;
						}
						
					} catch (ClassNotFoundException | NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}
	
	private void updByMasterIndex(ArrayList<MaestroIndex> returnMessage) {
		for(MaestroIndex mi : returnMessage){
			for(Archivo a : mi.getLiArchivo()) {
				this.mapPeer.put(a.getName(),mi.getOwner());
			}
		}
		this.hashToMasterIndex();
		
	}
	
	private void hashToMasterIndex() {
        Collection<String> values = this.mapPeer.values();
        ArrayList<String> listOfValues = new ArrayList<String>(values);
		ArrayList<MaestroIndex> result = new ArrayList<MaestroIndex>();
		Map<String, String> auxMap = new HashMap<>(this.mapPeer);
        
        for(String value : listOfValues) {
        	String arch;
        	ArrayList<Archivo> liArchivo = new ArrayList<Archivo>();
        	while((arch = getKey(auxMap, value)) != null) {
        		liArchivo.add(new Archivo(arch));
        	}
        	if(liArchivo.size()>0)
        		result.add(new MaestroIndex(value,liArchivo));
        }
		synchronized (this.peerData) {
			this.peerData = new ArrayList<MaestroIndex>(result);
		}
    }    

	//Guarda que borra (usar var aux)
	public String getKey(Map<String, String> map, String value) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				map.remove(entry.getKey());
				return entry.getKey();
			}
		}
		return null;
	}
	
	//Borrar todos las entradas dado un valor
	public String eraseByValue(Map<String, String> map, String value) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				map.remove(entry.getKey());
				return entry.getKey();
			}
		}
		return null;
	}
	
	public static void main(int args) {
		new Master1("127.0.0.1", args);
		MDC.remove("log.name");
	}

}
