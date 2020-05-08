package SDyPP.SDyPP_tp2_punto2.b;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class ClienteExtraccion {
	
	private int serverPort;

	public ClienteExtraccion(int serverPort) {
		this.serverPort = serverPort;
	}	
	
	public static void main(String[] args) {
		ClienteExtraccion cliente = new ClienteExtraccion(9090);
		
		cliente.extraer();
	}
	
	
	// Extrae un valor random
	public void extraer() {
		try {
			Socket s = new Socket("127.0.0.1", serverPort);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter writer = new PrintWriter(s.getOutputStream());				
			
			String line = reader.readLine();
			System.out.println("Server dice: " + line);
			
			double extrac = (new Random().nextInt(50) + 50)*10;
			System.out.println("Cliente extrae : " + extrac);
			writer.println(extrac);
			writer.flush();
			
			s.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
}
