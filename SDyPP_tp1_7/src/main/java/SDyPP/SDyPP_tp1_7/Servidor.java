package SDyPP.SDyPP_tp1_7;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Servidor {
	
	public static void main(String[] args) {
		
		try {
			Registry registry = LocateRegistry.createRegistry(10001);
			
			EjecutorTarea randomGen = new EjecutorTarea();
			
			IEjecutorTarea ejecutorTarea = (IEjecutorTarea) UnicastRemoteObject.exportObject(randomGen, 9999);
			
			registry.rebind("EjecutorTarea", ejecutorTarea);
			
			
			while (true) {
				
			}
				
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
