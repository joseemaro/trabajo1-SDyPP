package SDyPP.SDyPP_tp2_punto2.a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerDepositosHijo implements Runnable{

	private ServerDepositos server;
	
	private String cuenta;	
	
	private BufferedReader reader;
	
	private PrintWriter printer;	
	
	public ServerDepositosHijo(ServerDepositos server, String cuenta, Socket cliente) {
		super();
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
		
		try {			
			double saldoPrevio = getSaldoCuenta();
		
			printer.println("Ingresar monto a depositar. Saldo actual: " + saldoPrevio);
			
			// Leo el monto del socket
			String input = reader.readLine();

			// Parseo a double
			double monto = Double.parseDouble(input);
			
			// Deposito el monto en la cuenta
			double saldo = depositarCuenta(monto);
			
			printer.println("Monto depositado. Saldo restante: " + saldo);
			
			server.log("Saldo anterior: " + saldoPrevio + " - A depositar: " + monto + " - Nuevo Saldo: " + saldo);			
					
		} catch (NumberFormatException e) {
			printer.println("Se espera un numero.");
			e.printStackTrace();
		} catch (IOException e) {
			printer.println("Error en comunicacion con cliente.");
			e.printStackTrace();
		}		
	}

	private double depositarCuenta(double monto) {
		double montoActual = getSaldoCuenta();

		double nuevoSaldo = montoActual + monto;
		
		try {
			Thread.sleep(30);			
			BufferedWriter writer = new BufferedWriter(new FileWriter(cuenta));
			writer.write(Double.toString(nuevoSaldo));
			writer.flush();
			writer.close();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return 0; // Devuelvo 0 porque el archivo no existe
	}
	
}
