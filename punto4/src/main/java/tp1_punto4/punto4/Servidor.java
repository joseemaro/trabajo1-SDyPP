package tp1_punto4.punto4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



public class Servidor {

	public static void main(String[] args) {
		try {
			ServerSocket ss = new ServerSocket (9003);
			System.out.println( " OK FUNCIONANDO en el puerto 9003");
			
			while(true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Socket cliente = ss.accept();
				ServidorHijo sh = new ServidorHijo(cliente);
				Thread shThread = new Thread(sh);			
				shThread.start();
	
			}
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}

}
