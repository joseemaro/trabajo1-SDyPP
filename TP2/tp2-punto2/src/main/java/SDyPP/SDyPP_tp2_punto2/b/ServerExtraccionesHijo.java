package SDyPP.SDyPP_tp2_punto2.b;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerExtraccionesHijo implements Runnable{
	
	private ServerExtracciones server;
	
	private File archivoCuenta;
	
	private BufferedReader reader;
	
	private PrintWriter printer;

	public ServerExtraccionesHijo(ServerExtracciones server, File cuenta, Socket cliente) {
		super();
		this.server = server;		
		this.archivoCuenta = cuenta;
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
			synchronized (archivoCuenta) {
								
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
				
			}
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
				BufferedWriter writer = new BufferedWriter(new FileWriter(archivoCuenta));
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
			BufferedReader fileReader = new BufferedReader(new FileReader(archivoCuenta));	
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
