package Tp_1.tp1_punto5;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Server {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		try {
			// [STEP 1] - Create RMI Server {while true}
			Registry serverRMI = LocateRegistry.createRegistry(9200);
			
			ServerImplement si = new ServerImplement();
			ServerImplement si2 = new ServerImplement();
			
			// [STEP 3] - Export object as a service
			RemoteInt serviceA = (RemoteInt) UnicastRemoteObject.exportObject(si, 7880);
			//RemoteInt serviceB = (RemoteInt) UnicastRemoteObject.exportObject(si2, 7881);
			
			// [STEP 4] - vinculación "bind" de nombre de servicio a objeto
			serverRMI.rebind("vector-servicio", serviceA);
			//serverRMI.rebind("servicio", serviceB);
			System.out.println("servidor corriendo");
			while(true) {
				
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
