package SDyPP.SDyPP_tp2_punto2.a;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerDepositos{
	
	private String cuenta;
	
	private Logger logger;

	public ServerDepositos(String cuenta, String logFile, String loggerName) {
		super();
		this.cuenta = cuenta;
		this.logger = Logger.getLogger(loggerName);
		try {
			FileHandler handler = new FileHandler(logFile);
			handler.setFormatter(new SimpleFormatter());
			this.logger.addHandler(handler);			
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		ServerDepositos server = new ServerDepositos("cuenta.txt", "logDepositos.txt", "Logger");
		server.run();
	}
	
	
	
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(9000);
			System.out.println("Server depositos listening on port 9000");
			
			while (true) {
				Socket cliente = ss.accept();				
				ServerDepositosHijo hijo = new ServerDepositosHijo(this, cuenta, cliente);
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
