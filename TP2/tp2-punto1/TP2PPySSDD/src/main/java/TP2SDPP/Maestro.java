package TP2SDPP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

//Atiende a un cliente q se conecto contra el.
public class Maestro implements Runnable{
	private final Logger log = LoggerFactory.getLogger(Maestro.class);
	String ip;
	int port;
    String msg;
	Socket client;
	volatile ArrayList<MaestroIndex> peerData;
	ArrayList<String[]> connMasters;
    
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	
	
	public Maestro (String ip, int port, Socket client) {
		this.ip = ip;
		this.port = port;		
		this.client = client;
		this.peerData = new ArrayList<MaestroIndex>();
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Maestro(String ip, int port, Socket client, ArrayList<MaestroIndex> peerData, ArrayList<String[]> connMasters) {
		this.peerData = peerData;
		this.connMasters = connMasters;
		this.ip = ip;
		this.port = port;		
		this.client = client;
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		actualizarOtrosMaestros();
	}


	public void addPeer(String peer, ArrayList<Archivo> peerSharedData) {
		synchronized (this.peerData) {
			this.peerData.add(new MaestroIndex(peer,peerSharedData));
		}
	}


	public ArrayList<String> lookupMultipleArchivo(String name) {
		ArrayList<String> resultado = new ArrayList<String>();
		synchronized (this.peerData) {
			for (MaestroIndex entry : this.peerData) {
				if(entry.getLiArchivo() != null) {
					for(Archivo arch : entry.getLiArchivo()) {
						if(arch.getName().contains(name)) {
							resultado.add(entry.getOwner()+"//@t//"+arch.getName());
						}
					}
				}
			}
		}
		return resultado;
	}

	private void actualizar(String port) {
		try {
			String msg;
			ArrayList<Archivo> store = new ArrayList<Archivo>();
			boolean salir = false;
			String data = this.client.getInetAddress().getHostAddress()+":"+port;
			while(!salir){
				boolean existe = false;
				if((msg = inputChannel.readLine()) != null) {
					if(!msg.contains(".END")) {
						for(MaestroIndex m : this.peerData) {
							if(m.getLiArchivo() != null) {
								if(m.getOwner().contentEquals(data) && m.getLiArchivo().size()>0) {
									for(Archivo a : m.getLiArchivo()) {
										existe = a.getName().contentEquals(msg);
									}									
								}
							}else {
								m.setLiArchivo(new ArrayList<Archivo>());
							}
						}
						if(!existe) {
							log.info("[MASTER-"+this.port+"]: Guardando archivo: "+msg+" de "+data);
							store.add(new Archivo(msg));
						}
					}else {
						salir = true;
						addPeer(data,store);
						log.info("[MASTER-"+this.port+"]: Terminé de ejecutar una actualizacion. Servidor peer corriendo en: "+data);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void actualizarOtrosMaestros() {
		ArrayList<MaestroIndex> toSend = null;
		Socket sock = null;
		if(!this.connMasters.isEmpty()) {
			for(String[] str : this.connMasters) {
				try {
					sock = new Socket(str[0],Integer.valueOf(str[1]));
					PrintWriter outChannel = new PrintWriter (sock.getOutputStream(), true);
					outChannel.println("actualizarServerMaster="+this.ip+":"+this.port);
					Thread.sleep(200);
					outChannel.println("sending");
					ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
					toSend = this.peerData;
					os.writeObject(toSend);	     
					os.flush();
				} catch (Exception e) {
					log.error("Hubo un error al actualizar Master "+str[0]+":"+str[1]);
					log.error("Error: "+e.getMessage());
					sock = null;
				}
			}
		}
	}
	
	@Override
	public void run() {
		MDC.put("log.name", Maestro.class.getSimpleName().toString()+"-"+this.port);
		String msg = "";
		String[] msgParced;
		boolean end = false;
		try {
			while(!end && this.client.isConnected()) {
				msg = this.inputChannel.readLine();
				actualizarWorker();
				if(!msg.isEmpty()) {
					if(msg.split("=").length > 0) {
							msgParced = msg.split("=");
							switch (msgParced[0]) {
							case "buscar":
								if(msgParced.length >=1) {
									ArrayList<String> busqueda = lookupMultipleArchivo(msgParced[1]);
									this.outputChannel.flush();
									if (!(busqueda.isEmpty())) {
										for(String m : busqueda) {
											this.outputChannel.println(m);
										}
										this.outputChannel.println(".END");
									}else {
										this.outputChannel.println("no existe");
									}
									msgParced = null;
								}
								break;
							case "cerrarConn":
								String s = this.client.getInetAddress().getHostAddress()+":"+msgParced[1];
								log.info("[MASTER-"+this.port+"]: Cerrando conexion con "+s);
								bajaPeer(s);
								actualizarOtrosMaestros();
								actualizarWorker();
								this.client.close();
								end = true;
								break;
							case "peerServerPortOn":
								actualizar(msgParced[1]);
								actualizarOtrosMaestros();
								break;
							default:
								break;
						}	
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void bajaPeer(String s) {
		try {
			Socket svMaster = new Socket(this.ip,this.port);
			PrintWriter outChannel = new PrintWriter (svMaster.getOutputStream(), true);
			outChannel.println("bajaPeer");
			Thread.sleep(200);
			outChannel.println(s);
			svMaster.close();
		} catch (Exception e) {
			log.error("Error: "+e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void actualizarWorker() {
		ArrayList<MaestroIndex> returnMessage = null;
		try {
			Socket svMaster = new Socket(this.ip,this.port);
			PrintWriter outChannel = new PrintWriter (svMaster.getOutputStream(), true);
			outChannel.println("actualizarWorker");
			Thread.sleep(200);
			ObjectInputStream is = new ObjectInputStream(svMaster.getInputStream());
			returnMessage = (ArrayList<MaestroIndex>) is.readObject();
			this.peerData = returnMessage;
			svMaster.close();
		} catch (Exception e) {
			log.error("Error: "+e.getMessage());
		}
		
	}

}
