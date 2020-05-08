package SDyPP.SDyPP_tp2_punto2.b;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLauncher {

	
	public static void main(String[] args) {
		
		File cuenta = new File("cuenta.txt");
		
		Logger logger = Logger.getLogger("Logger");
		
		FileHandler handler;
		try {
			handler = new FileHandler("log.txt");
			handler.setFormatter(new SimpleFormatter());		 
			logger.addHandler(handler);	
			
			ServerDepositos serverDepositos = new ServerDepositos(cuenta, logger);

			Thread t1 = new Thread(serverDepositos);
			
			ServerExtracciones serverExtracciones = new ServerExtracciones(cuenta, logger);
			
			Thread t2= new Thread(serverExtracciones);
			
			t1.start();
			t2.start();
			
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
}
