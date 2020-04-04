package Tp_1.punto6;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;



public class Cliente  {



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			Registry clientRMI = LocateRegistry.getRegistry("127.0.0.1", 4000);
			String[] services = clientRMI.list();		
			RemoteInt ri = (RemoteInt) clientRMI.lookup(services[0]);
			
			//creo vectores
			int[] v1 = { 1 ,2, 3 , 4 , 5 };
			int[] v2 = { 1 ,2, 3 , 4 , 5 };
			//muestro vectores
			System.out.println(mostrarVector(v1,1));
			System.out.println(mostrarVector(v2,2));
			
			//creo vector y llamo a sumar los 2 vectores anteriores
			int v3[];			
			v3 = ri.sumarVector(v1, v2);
			System.out.println("El resultado de la suma es= ");
			System.out.println(mostrarVector(v3,3));
		
			/////prueba de pasaje por referencia/////////
			//le devuelvo los valores
			Vector v4 = new Vector(v1);
			Vector v5 = new Vector(v2);
			System.out.println("introduciendo error..., sustituimos los valores de los vectores por 0");
			System.out.println("Los resultados obtenidos son= ");

			int vr[] = ri.sumarVect(v4, v5);
			//muestra vectores
			System.out.println("El resultado de la suma es= ");
			System.out.println(mostrarVector(vr,3));
			System.out.println("los vectores siguen igual= ");
			System.out.println(mostrarVect(v4,1));
			System.out.println(mostrarVect(v5,2));
			
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		
		
	}

	private static String mostrarVect(Vector v5, int j) {
		// TODO Auto-generated method stub
		String msg="Vector numero " + j + " = " ;
		int aux[]= v5.getV1();
		for (int i=0; i<aux.length; i++) {
			msg+= aux[i] + ",";
		}
		return msg;
	}
		

	private static String mostrarVector(int[] v1, int j) {
		String msg="Vector numero " + j + " = " ;
		for (int i=0; i<v1.length; i++) {
			msg+= v1[i] + ",";
		}
		return msg;
	}

}
