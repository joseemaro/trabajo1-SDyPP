package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import abst.IClimaManager;

public class Client {

	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9200);		
						
			IClimaManager climaManager = (IClimaManager) registry.lookup("ClimaManager");
			
			int temp = climaManager.getTemperatura();
			System.out.println("Segun el server, la temperatura es " + temp + " grados.");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Ups, hubo un error.");
		}
	}
}
