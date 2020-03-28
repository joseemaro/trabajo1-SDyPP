package Tp_1.punto6;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;



public class Cliente {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			Registry clientRMI = LocateRegistry.getRegistry("127.0.0.1", 9300);
			String[] services = clientRMI.list();		
		try {
			RemoteInt ri = (RemoteInt) clientRMI.lookup(services[0]);
			
			int[] v1 = { 1 ,2, 3 , 4 , 5 };
			int[] v2 = { 1 ,2, 3 , 4 , 5 };
			System.out.println(mostrarVector(v1,1));
			System.out.println(mostrarVector(v2,2));
			
			int v3[];
			v3 = ri.sumarVector(v1, v2);
			System.out.println("El resultado de la suma es= ");
			System.out.println(mostrarVector(v3,3));
		
			
			v1 = ri.devolverVector(v1);
			v2 = ri.devolverVector(v2);
			System.out.println("introduciendo error..., sustituimos los valores de los vectores por 0");
			System.out.println("Los resultados obtenidos son= ");
			System.out.println(mostrarVector(v1,1));
			System.out.println(mostrarVector(v2,2));
			v3 = ri.sumarVector(v1, v2);
			System.out.println("El resultado de la suma es= ");
			System.out.println(mostrarVector(v3,3));
			
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			System.out.println("uno");
			e.printStackTrace();
		}	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.out.println("dos");
			e.printStackTrace();
		}
		
		
	}

	private static String mostrarVector(int[] v1, int j) {
		String msg="Vector numero " + j + " = " ;
		for (int i=0; i<v1.length; i++) {
			msg+= v1[i] + ",";
		}
		return msg;
	}

}
