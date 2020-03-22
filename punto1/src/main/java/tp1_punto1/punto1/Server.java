package tp1_punto1.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hello world!
 *
 */
public class Server 
{
    public static void main( String[] args )
    {
       try {
    	// Primero se crea el servidor.
		ServerSocket ss= new ServerSocket(9002);
		System.out.println("OK conectado en el puerto 9002  - ");
		
		while(true) {
			//acepto clientes
			Socket cliente = ss.accept();
			System.out.println(" CLIENTE RECIBIDO: "+cliente.getInetAddress().getCanonicalHostName() + "Puerto : " +cliente.getPort());

			
			//establecer canal tcp, 2 canales, de entrada y salida	
			BufferedReader canalEntrada = new BufferedReader(new InputStreamReader ( cliente.getInputStream()));
			PrintWriter canalSalida= new PrintWriter(cliente.getOutputStream(), true); //true es para que cuando haga un canal de salida haga flush solo


			//leer del cliente
			String msgEntrada= canalEntrada.readLine();
			
			
			msgEntrada += " El mensaje del cliente fue = " + msgEntrada;
			
			//respuesta al cliente
			canalSalida.println(msgEntrada);
			
			//cerrar la conexion
			cliente.close();
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.out.println("ERROR al conectarse al puerto 9002");
	}
    }
}
