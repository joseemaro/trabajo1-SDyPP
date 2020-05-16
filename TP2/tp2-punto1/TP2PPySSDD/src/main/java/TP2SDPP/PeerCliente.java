package TP2SDPP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
//Envia archivo disponible a Maestro cpnectado, anuncia un menu para poder interactuar con el Master1 y guarda el archivo descargado, renombrandolo si asi lo requiriera.
public class PeerCliente implements Runnable {
	String peerIp;
	int peerServerPort;
	String directory;
	private final Logger log = LoggerFactory.getLogger(PeerCliente.class);
	Socket connMaestro;
	ArrayList<Archivo> liArchivos;
	private Scanner sc; 
	
	public PeerCliente(Socket connMaestro, int peerServerPort, ArrayList<Archivo> liArchivos, String directory) {
		this.connMaestro = connMaestro;
		this.peerIp = connMaestro.getLocalAddress().getCanonicalHostName();
		this.peerServerPort = peerServerPort;
		this.directory = directory;
		log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Cargando mis archivos para compartir...");
		this.liArchivos = liArchivos;
	}
	private void menuDescargar() {
		ArrayList<String> response;
		int index = 0;
		String read;
		boolean termino = false;
		System.out.println("Buscar: ");
		read = sc.nextLine();
		while(read.isEmpty()) {
			System.out.println("Error!\nDebe ingresar al menos un caracter\nBuscar:");
			read = sc.nextLine();
		}
		response = buscar(read);
		if (response != null) {
			String[] parced = null;
			System.out.println("Seleccione el nro indice de el que desea descargar o X para salir");
			for (String res : response) {
				parced = res.split("//@t//");
				System.out.println(index+". "+parced[1]+"(on "+parced[0]+")");
				index++;
				parced = null;
			}
			System.out.println("Ingrese el indice de el arhivo a descargar: ");
			read = sc.nextLine();
			while(!termino) {
				if(read.matches("\\d+")){
					if ((Integer.valueOf(read) >= 0)&&(Integer.valueOf(read) < index)){
						termino = true;
						index = 0;
						String sel = response.get(Integer.valueOf(read));
						parced = sel.split("//@t//");
						Archivo a = descargar(parced[0].split(":"), parced[1]);	
						log.info("Prueba de que funciono: "+a.getName()+"\nContent: "+a.getContent());
						guardar(a);
					}else {
						System.out.println("Error!\nIngrese un numero valido o X para salir:");
						read = sc.nextLine();
					}
				
				}else {
					if(read.toLowerCase().contentEquals("x")) {
						termino = true;
					}else {
						System.out.println("Error!\nIngrese un numero valido o X para salir:");
						read = sc.nextLine();
					}
				}
			}
		}
	}
	private void menu() {
		sc = new Scanner(System.in);
		String opt;
		boolean salir = false;
		while (!salir) {
			System.out.println("========================\n"
					+ "1.Buscar archivo\n"
					+ "2.Ver mis archivos\n"
					+ "\n"
					+ "4.Salir\n"
					+ "========================");
			opt = sc.nextLine();
			switch (opt) {
			case "1": 
				menuDescargar();
				break;
			case "2": 
				verMisArchivos();
				break;
			case "4": 
				salir = true;
				try {
					PrintWriter outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);
					outputChannel.println("cerrarConn="+this.peerServerPort);
					this.connMaestro.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			default: System.out.println("Opcion invalida.");
			}
		}		
	}

	
	private void verMisArchivos() {
		for(Archivo a  : this.liArchivos) {
			System.out.println(this.liArchivos.indexOf(a)+". "+a.getName());
		}
	}
	
	private void guardar(Archivo a) {
		File file = new File(this.directory+"/"+a.name);
		if(!file.exists()) {
			try (FileOutputStream stream = new FileOutputStream(this.directory+"/"+a.name)) {
				stream.write(a.getContent());
				this.liArchivos.add(a);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else {
			log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Ya existe un archivo con ese nombre. Renombrando...");
			try (FileOutputStream stream = new FileOutputStream(this.directory+"/"+"Copy "+a.name)) {
				stream.write(a.getContent());
				this.liArchivos.add(new Archivo("Copy "+a.getName()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		givePeerData();
	}

	private Archivo descargar(String[]connData,String archivo) {
		try {
			Socket s = new Socket (connData[0], Integer.valueOf(connData[1]));
			log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Estableciendo conexion con: "+connData[0]+":"+connData[1]);
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (s.getOutputStream(), true);

			log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Conexion establecida. Enviando peticion de descarga de: "+archivo);
			outputChannel.println("descargar="+archivo);
			String msgFromServer= inputChannel.readLine();
			if(msgFromServer.contains("error")) {
				log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Algo salió mal. No se pudo descargar");
			    s.close();
			}else if(msgFromServer.contains("sending")){
				try {
				    Archivo returnMessage = null;
					ObjectInputStream is = new ObjectInputStream(s.getInputStream());
					returnMessage = (Archivo) is.readObject();
					log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Recibiendo contenido...");
				    System.out.println("Archivo que descargue es:" + returnMessage.getName());
				    is.close();
				    return returnMessage;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}else {
				log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Algo salió realmente mal.");
			    s.close();
			}				
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<String> buscar(String query) {
		ArrayList<String> response = new ArrayList<String>();
		boolean termino = false;
		try {
			log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Cliente conectado al master.Buscando archivos...");
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (this.connMaestro.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);
			outputChannel.println("buscar="+query);
			String msgFromServer = null;
			while(!termino) {
				if((msgFromServer = inputChannel.readLine()) != null && !msgFromServer.contentEquals(".END")) {
					if (msgFromServer.contentEquals("no existe") || response == null) {
						log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: La busqueda '"+query+"' no produjo resultados");
						termino = true;
						return null;
					}else {
						if(msgFromServer.contains("//@t//"))
						response.add(msgFromServer);
					}
				}else {
					termino = true;
					return response;
				}
			}				
		} catch (IOException e) {
			log.error("Se perdio la conexion con el servidor.");
			log.error("Porfavor intente mas tarde");
			this.menu();
		}
		return null;
	}



	private void givePeerData() {
		try {
			log.info("[PEER-CLIENT-"+this.peerIp+":"+this.peerServerPort+"]: Cliente conectado al master. Enviando archivos disponibles");
			PrintWriter outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);
			if(this.liArchivos.isEmpty()) {
				outputChannel.println("peerServerPortOn="+this.peerServerPort);
				outputChannel.println(".END");
			}else{
				outputChannel.println("peerServerPortOn="+this.peerServerPort);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(this.liArchivos != null) {
					for (Archivo archivo : this.liArchivos) {
						outputChannel.println(archivo.getName());
					}
					outputChannel.println(".END");
				}else {
					outputChannel.println(".END");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		Thread myThread = Thread.currentThread();
		MDC.put("log.name", PeerCliente.class.getSimpleName().toString()+"-"+this.peerServerPort+"-"+myThread.getId());
		givePeerData();
		menu();
		myThread.interrupt();//Not Working
		MDC.remove("log.name");
	}

}
