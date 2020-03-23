package tp1_punto4.punto4;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServidorHijo implements Runnable{
	Socket cliente;
	
	public ServidorHijo (Socket cliente) {
		this.cliente = cliente;
	}

	public void run() {
		
		try {
			
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (this.cliente.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (this.cliente.getOutputStream(), true);
			String msgEntrada = "";
			msgEntrada = canalEntrada.readLine();
			
			ArrayList<String> clientes = new ArrayList<String>();
			clientes.add(msgEntrada);
 			canalSalida.println("me llego" + msgEntrada);
			
 			
 			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
 			
 			System.out.println("lista de mensajes recibidos" + clientes);
 			
 			msgEntrada="";
 			msgEntrada=canalEntrada.readLine();
 			
 			if (msgEntrada.equalsIgnoreCase("ack") ) {
 			clientes.remove(clientes.size()-1);
 			System.out.println("se recibio el ack del cliente, se elimina el mensaje");
 			}
 			
 			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
 			
 			
 			this.cliente.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}
