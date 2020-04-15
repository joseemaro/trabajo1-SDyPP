package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

import TP1.Punto3.Funcion;
import TP1.Punto3.ListaMensajes;
import TP1.Punto3.Mensaje;

/**
 * Se ejecuta en un hilo del server. Es el que atiende 
 * las peticiones de los clientes. 
 * @author mariano
 *
 */
public class GestorMensajes implements Runnable {
		
	private Socket cliente;
	
	private BufferedReader canalEntrada;
	
	private PrintWriter canalSalida;
	
	private ListaMensajes listaMensajes;
	
	public GestorMensajes(Socket cliente, ListaMensajes listaMensajes) throws IOException {
		this.cliente = cliente;	
		
		this.canalEntrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
		
		this.canalSalida = new PrintWriter(cliente.getOutputStream(), true);
		
		this.listaMensajes = listaMensajes;
	}

	public void run() {	
		try {
			while (true) {
			String input = canalEntrada.readLine();			
			Gson gson = new Gson();
			
			// Obtengo una Funcion a partir del input
			Funcion f = gson.fromJson(input, Funcion.class);			
			
			// Si llego una f valida
			if (f != null) {
				// Verifico que tipo de funcion es
				switch (f.getId()) {
					case 1: almacenarMensaje(f);
						break;
					case 2: verMensaje(f);
						break;
					default:
						break;				
					}
				}			
			}
			
		} catch (IOException e) {
			System.out.println("El cliente debe haberse caido");
		}
	}

	private void verMensaje(Funcion f) {
		String destino = f.getCuerpo();
		
		List<Mensaje> mensajesRta = this.listaMensajes.getMensajesPara(destino);
		
		Gson gson = new Gson();
		
		String output = gson.toJson(mensajesRta);
		
//		// Si no hay mensajes
//		if (mensajesRta.isEmpty()) {
//			output = "No hay mensajes para ese destinatario!";
//		}
				
		canalSalida.println(output);
	}

	private void almacenarMensaje(Funcion f) {
		Gson gson = new Gson();
		Mensaje m = gson.fromJson(f.getCuerpo(), Mensaje.class);
		
		this.listaMensajes.add(m);
		
		System.out.println("Mensaje almacenado.");
		
		canalSalida.println("Mensaje almacenado correctamente!");
		
	}
}
