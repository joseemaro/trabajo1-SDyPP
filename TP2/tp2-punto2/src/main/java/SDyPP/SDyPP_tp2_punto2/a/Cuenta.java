package SDyPP.SDyPP_tp2_punto2.a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Cuenta {

//	private String path;
	private BufferedReader reader;
	private BufferedWriter writer;	
	
	public Cuenta(String path) {
		super();
//		this.path = path;
		
		try {
			this.writer = new BufferedWriter(new FileWriter(path));		
			
			this.writer.write("0"); // Inicializa la cuenta en 0
			this.writer.flush();
			
			this.reader = new BufferedReader(new FileReader(path));		
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public double getSaldo() {
		try {
			String in = this.reader.readLine();
			double value = Double.parseDouble(in);
			return value;
		} catch (IOException e) {
			System.out.println("Error IO de archivo.");
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("Error en el formato del archivo.");
		}
		return -1;		
	}
	
	/**
	 * Deposita el monto en la cuenta
	 * @param monto Monto a depositar
	 * @return El saldo de la cuenta
	 */
	public double depositar(double monto) {
		double montoActual = getSaldo();

		double nuevoMonto = montoActual + monto;
		
		try {
			this.writer.write(Double.toString(nuevoMonto));
			this.writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getSaldo();		
	}
	
	/**
	 * Extrae el monto de la cuenta
	 * @param monto Monto a extraer
	 * @return El saldo de la cuenta
	 */
	public double extraer(double monto) {
		double montoActual = getSaldo();

		double nuevoMonto = montoActual - monto;
		
		if (nuevoMonto >= 0) {
		
			try {
				this.writer.write(Double.toString(nuevoMonto));
				this.writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		return getSaldo();
		
	}
	
}
