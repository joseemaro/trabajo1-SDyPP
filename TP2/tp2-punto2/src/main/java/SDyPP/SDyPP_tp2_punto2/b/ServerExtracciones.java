package SDyPP.SDyPP_tp2_punto2.b;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerExtracciones implements Runnable{
	
	private File cuenta;
	
	private Logger logger;

	public ServerExtracciones(File cuenta, Logger logger) {
		super();
		this.cuenta = cuenta;
		this.logger = logger;
		
	}	

	
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(9090);
			System.out.println("Server extracciones listening on port 9090");
			
			while (true) {
				Socket cliente = ss.accept();
				
				ServerExtraccionesHijo hijo = new ServerExtraccionesHijo(this, cuenta, cliente);
				Thread t = new Thread(hijo);
				t.start();
			}	
			
		} catch (IOException e) {			
			
		}		
	}
	
	protected void log(String mensaje) {
		long milis = System.currentTimeMillis();
		mensaje = "(" + milis + ")---> " + mensaje;
		this.logger.log(Level.INFO, mensaje);
	}	
	
}
