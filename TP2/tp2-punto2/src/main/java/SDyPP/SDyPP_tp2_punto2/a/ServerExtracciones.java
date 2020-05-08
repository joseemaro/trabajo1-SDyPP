package SDyPP.SDyPP_tp2_punto2.a;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerExtracciones{
	
	private String cuenta;
	
	private Logger logger;

	public ServerExtracciones(String cuenta, String logFile, String loggerName) {
		super();
		this.cuenta = cuenta;
		this.logger = Logger.getLogger(loggerName);
		try {
			FileHandler handler = new FileHandler(logFile);
			handler.setFormatter(new SimpleFormatter());
			this.logger.addHandler(handler);			
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		ServerExtracciones server = new ServerExtracciones("cuenta.txt", "logExtracciones.txt", "Logger");		
		server.run();		
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
