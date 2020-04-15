package tp1_punto2.punto2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Cliente {

	public static void main(String[] args) {
		
		
		try {
			
			Socket s = new Socket ("127.0.0.1", 9000);
			
			
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			
			
			canalSalida.println( "MSG DEL CLIENTE: "+s.getLocalPort());
			
			
			String msgRespuesta = canalEntrada.readLine();
			
			System.out.println(" MSG RESPUESTA: "+msgRespuesta);
			
			
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

