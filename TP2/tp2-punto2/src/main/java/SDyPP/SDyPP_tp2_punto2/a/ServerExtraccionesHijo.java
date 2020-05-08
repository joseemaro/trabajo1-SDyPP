package SDyPP.SDyPP_tp2_punto2.a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerExtraccionesHijo implements Runnable{
	
	private ServerExtracciones server;
	
	private String cuenta;	
	
	private BufferedReader reader;
	
	private PrintWriter printer;

	public ServerExtraccionesHijo(ServerExtracciones server, String cuenta, Socket cliente) {
		
		this.server = server;
		this.cuenta = cuenta;
		
		try {
			
			this.reader = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			
			this.printer = new PrintWriter(cliente.getOutputStream(), true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		
		printer.println("Ingresar monto a extraer.");
			
		try {
			
			double saldoPrevio = getSaldoCuenta();
			
			String input = reader.readLine();
			
			System.out.println("Cliente extrae: " + input);
			
			// Leo el monto del socket
			double monto = Double.parseDouble(input);
			
			// Deposito el monto en la cuenta
			double saldo = extraerCuenta(monto);		
			
			if (saldo == saldoPrevio) {
				printer.println("Saldo insuficiente. Saldo: " + saldo);
			}
			else {
				printer.println("Monto extraído. Saldo restante: " + saldo);				
			}
			
			server.log("Saldo anterior: " + saldoPrevio + " - A extraer: " + monto + " - Nuevo Saldo: " + saldo);			
		
		} catch (NumberFormatException e) {
			printer.println("Se espera un numero.");
			e.printStackTrace();
		} catch (IOException e) {
			printer.println("Error en comunicacion con cliente.");
			e.printStackTrace();
		}		
		
	}

	private double extraerCuenta(double monto) {
		double montoActual = getSaldoCuenta();

		double nuevoMonto = montoActual - monto;
		
		if (nuevoMonto >= 0) {
		
			try {
				Thread.sleep(60);
				BufferedWriter writer = new BufferedWriter(new FileWriter(cuenta));
				writer.write(Double.toString(nuevoMonto));
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		return getSaldoCuenta();
	}

	private double getSaldoCuenta() {
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(cuenta));	
			String in = fileReader.readLine();
			fileReader.close();			
			double value = Double.parseDouble(in);
			return value;
		} catch (IOException e) {
		
		} catch (NumberFormatException e) {
			
		}
		return 0; // Si no existe el archivo, devuelve 0
	}

}
