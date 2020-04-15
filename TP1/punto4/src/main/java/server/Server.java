package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import TP1.Punto4.ListaMensajes;

/**
 * Servidor de mensajes. Escucha en el puerto 18000.  
 *
 */
public class Server
{
		
    public static void main( String[] args )
    {    	
    	try {
			ServerSocket ss = new ServerSocket(18000);
			System.out.println("Server comenzo ok...");
			JOptionPane.showMessageDialog(null, "Arranca server");
			
			ListaMensajes listaMensajes = new ListaMensajes();
			
			while (true) {
			
				Socket cliente = ss.accept();
				
				GestorMensajes gestor = new GestorMensajes(cliente, listaMensajes);
				
				Thread hilo = new Thread(gestor);
				
				hilo.start();			
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error en comunicacion con cliente");
			
		}
    	
    	
    	
    }
}
