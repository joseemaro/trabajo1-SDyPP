package tp1_punto1.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Cliente {

	public static void main(String[] args) {
		try {
			//conectarse con el servidor
			Socket s = new Socket("127.0.0.1", 9002);
			
			//establecer canal
			BufferedReader canalEntrada = new BufferedReader(new InputStreamReader ( s.getInputStream()));
			PrintWriter canalSalida= new PrintWriter(s.getOutputStream(), true); //true es para que cuando haga un canal de salida haga flush solo

			// Paso 3 - Enviar petición al servidor 
			canalSalida.println( "MSG DEL CLIENTE: Hola me llamo pinocho - ");
						
			// Paso 4 - Recibir la respuesta
			String msgRespuesta = canalEntrada.readLine();
			
			
			System.out.println(msgRespuesta);
						
			// Paso 5 - Cerrar la conexión
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
