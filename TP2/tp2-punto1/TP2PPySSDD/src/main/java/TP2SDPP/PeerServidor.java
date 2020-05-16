package TP2SDPP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
//Espera conexciones nuevas y una vez obtenida una nueva conecion  lanza al trabajador para atender al cliente.
public class PeerServidor implements Runnable{
	String peerIp;
	int peerPort;
	ArrayList<Archivo> liArchivos;
	private final Logger log = LoggerFactory.getLogger(PeerServidor.class);
	String directory;
	
	public PeerServidor (String ip, int port, ArrayList<Archivo> liArchivos, String directory) {
		this.peerPort = port;
		this.peerIp = ip;
		this.liArchivos = liArchivos;
		this.directory = directory;
	}


	@Override
	@SuppressWarnings("resource")
	public void run() {
		try {
			MDC.put("log.name", PeerServidor.class.getSimpleName().toString()+"-"+this.peerPort+"-"+Thread.currentThread().getId());
			ServerSocket ss = new ServerSocket (this.peerPort);
			log.info("[SERVER PEER-"+this.peerPort+"]: ON");
			int counter = 0;
			while (true) {
				Socket client = ss.accept();
				counter++;
				log.info("[SERVER PEER-"+this.peerPort+"] Establecio conexion con el cliente nro: "+counter);
				log.info("[SERVER PEER-"+this.peerPort+"] Info Cliente: "+client.getInetAddress()+":"+client.getPort());
				//Thread: Serve petition
				Trabajador worker = new Trabajador (this.liArchivos,client, directory);
				Thread tWorker= new Thread (worker);
				tWorker.start();
			}
		} catch (IOException e) {
			System.out.println(" Socket on port "+peerPort+" is used ");
		}
	}
}
