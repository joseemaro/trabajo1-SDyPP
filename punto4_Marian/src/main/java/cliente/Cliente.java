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

import TP1.Punto4.Funcion;
import TP1.Punto4.Mensaje;
import TP1.Punto4.TiposFuncion;

public class Cliente {
	
	private static Scanner scanner;
	private static BufferedReader canalEntrada;
	private static PrintWriter canalSalida;
	private static String destino;
	
	
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
							int cantidad = mostrarMensajesRecibidos(canalEntrada.readLine());
							if (cantidad > 0) {
								int subOp = mostrarMenuBorrarMensajes();
								switch (subOp) {
									case 0: 
										borrarMensajes();
										JOptionPane.showMessageDialog(null, canalEntrada.readLine() + "\n\n");		
										break;
									default: 
										mantenerMensajes();
										break;
								}
							}
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




	/**
	 * Informa al server que borre los msjs
	 */
	private static void borrarMensajes() {
		Funcion f = new Funcion();
		
		f.setId(TiposFuncion.BORRAR_MSJ);
		f.setCuerpo(destino);
		
		Gson gson = new Gson();
		
		String output = gson.toJson(f);
		
		canalSalida.println(output);
	}

	/**
	 * Informa al server que mantenga los msjs
	 */
	private static void mantenerMensajes() {		
		Funcion f = new Funcion();
		
		f.setId(TiposFuncion.MANTENER_MSJ);
		f.setCuerpo(destino);
		
		Gson gson = new Gson();
		
		String output = gson.toJson(f);
		
		canalSalida.println(output);
	}



	private static void nuevoMensaje() {
		Mensaje m = new Mensaje();
		
		m.setOrigen(JOptionPane.showInputDialog(null, "Origen:"));
		
		m.setDestino(JOptionPane.showInputDialog(null, "Destino:"));
		
		m.setCuerpo(JOptionPane.showInputDialog(null, "Cuerpo:"));
		
		Gson gson = new Gson();
		String msg = gson.toJson(m);
		
		Funcion f = new Funcion();
		f.setId(TiposFuncion.GUARDAR_MSJ);
		f.setCuerpo(msg);	
		
		String output = gson.toJson(f);
		canalSalida.println(output);		
	}

	
	private static void verMensajes() {
		destino = JOptionPane.showInputDialog(null, "Destino de los mensajes: ");
		
		Funcion f = new Funcion();
		f.setId(TiposFuncion.VER_MSJS);
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
	

	private static int mostrarMensajesRecibidos(String input) {
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
		return mensajes.size();
		
	}


	private static int mostrarMenuBorrarMensajes() {
		String msg = "";		
		msg+=("[1] Mantener mensaje/s \n");		
		msg+=("[0] Eliminar mensaje/s \n");
		msg+=("Opcion: \n");	
		try {
			return Integer.parseInt(JOptionPane.showInputDialog(null, msg, "Eliminar mensaje/s", JOptionPane.PLAIN_MESSAGE));
		}
		catch (Exception e) {
			return mostrarMenuPrincipal();
		}
	}
}
