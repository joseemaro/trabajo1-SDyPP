package tp1_punto4.punto4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Cliente 
{
    public static void main( String[] args )
    {
    	try {
			
    		
    		Socket s = new Socket ("127.0.0.1", 9003);
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			
			canalSalida.println( " HOLA QUE TAL es el mensaje del cliente Puerto=  "+ s.getLocalPort());
			String msgRespuesta ="";
			msgRespuesta = canalEntrada.readLine();
			
			System.out.print("el servidor dice: "+msgRespuesta);
			
			canalSalida.println("ack");
			
			s.close();
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
    }
}
