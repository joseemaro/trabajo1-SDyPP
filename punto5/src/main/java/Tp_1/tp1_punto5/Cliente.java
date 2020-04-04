package Tp_1.tp1_punto5;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;



public class Cliente {
		
	public static void main(String[] args) {
		
		
		Registry clientRMI;
		try {
			clientRMI = LocateRegistry.getRegistry("127.0.0.1", 9200);
			String[] services = clientRMI.list();		
			RemoteInt ri = (RemoteInt) clientRMI.lookup(services[0]);
			
			String ciudad;
			Scanner entrada = new Scanner(System.in);
			System.out.println("Introduzca su ciudad a continuacion...");
			ciudad = entrada.nextLine();
			String msg= ri.devolverClima(ciudad);
			System.out.println(msg);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
