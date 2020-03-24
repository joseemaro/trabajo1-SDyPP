package cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import TP1.Punto3.Funcion;
import TP1.Punto3.Mensaje;

public class Cliente {
	
	private static Scanner scanner;
	private static BufferedReader canalEntrada;
	private static PrintWriter canalSalida;
	
	
	public static void main(String[] args) {		
		try {
			Socket s = new Socket("127.0.0.1", 18000);
			
			canalEntrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			canalSalida = new PrintWriter(s.getOutputStream(), true);

			scanner = new Scanner(System.in);
			
			int op = 1;			
			while (op != 0) {
				op = mostrarMenuPrincipal();				
				switch (op) {
					case 1: nuevoMensaje();
							JOptionPane.showMessageDialog(null, canalEntrada.readLine() + "\n\n");				
						break;
					case 2: verMensajes();
							mostrarMensajesRecibidos(canalEntrada.readLine());
						break;
					default:
						break;
				}
			}
			
			// Sale del while y cierro la conexion
			s.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error", "Error en el server.", JOptionPane.ERROR_MESSAGE);
		}		
	}



	private static void nuevoMensaje() {
		Mensaje m = new Mensaje();
		
		m.setOrigen(JOptionPane.showInputDialog(null, "Origen:"));
		
		m.setDestino(JOptionPane.showInputDialog(null, "Destino:"));
		
		m.setCuerpo(JOptionPane.showInputDialog(null, "Cuerpo:"));
		
		Gson gson = new Gson();
		String msg = gson.toJson(m);
		
		Funcion f = new Funcion();
		f.setId(1);
		f.setCuerpo(msg);	
		
		String output = gson.toJson(f);
		canalSalida.println(output);		
	}

	
	private static void verMensajes() {
		String destino = JOptionPane.showInputDialog(null, "Destino de los mensajes: ");
		
		Funcion f = new Funcion();
		f.setId(2);
		f.setCuerpo(destino);
		
		Gson gson = new Gson();
		
		String output = gson.toJson(f);
		
		canalSalida.println(output);
	}
	
	private static int mostrarMenuPrincipal(){
		String msg = "";
		msg += "Menu\n";
		msg+=("[1] Nuevo mensaje \n");
		msg+=("[2] Ver mensajes \n");
		msg+=("[0] Salir \n");
		msg+=("Opcion: \n");	
		try {
			return Integer.parseInt(JOptionPane.showInputDialog(null, msg, "Menu Principal (cliente)", JOptionPane.PLAIN_MESSAGE));
		}
		catch (Exception e) {
			return mostrarMenuPrincipal();
		}
	}
	

	private static void mostrarMensajesRecibidos(String input) {
		Gson gson = new Gson();
		
		List<Mensaje> mensajes = gson.fromJson(input, new TypeToken<List<Mensaje>>() {}.getType()); 
		
		String msg = "";
		for (Mensaje m : mensajes) {
			msg += m.toString() + "\n";
		}
		
		if (mensajes.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No hay mensajes!");
		}
		else {
			JOptionPane.showMessageDialog(null, msg, "Mensajes recibidos", JOptionPane.PLAIN_MESSAGE);
		}
	}
}
