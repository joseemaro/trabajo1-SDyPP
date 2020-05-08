package SDyPP.SDyPP_tp2_punto2.b;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class ClienteDeposito {
	
	private int serverPort;
	
	public ClienteDeposito(int serverPort) {		
		this.serverPort = serverPort;
	}
	
	
	public static void main(String[] args) {
		ClienteDeposito cliente = new ClienteDeposito(9000);
		
		cliente.depositar();
	}
	
	
	
	public void depositar() {
		// Abre un socket y deposita valor random
		
		try {			
			Socket s = new Socket("127.0.0.1", serverPort);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter writer = new PrintWriter(s.getOutputStream(), true);				
			
			String line = reader.readLine();
			System.out.println("Server dice: " + line);
			
			double depo = (new Random().nextInt(50) + 50)*10;
			System.out.println("Cliente deposita : " + depo);
			writer.println(depo);
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
