package SDyPP.SDyPP_tp1_7;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class Cliente {

	public static void main(String[] args) {
		try {
			// Obtengo el registro
			Registry registry = LocateRegistry.getRegistry("127.0.0.1", 10001);
		
			// Hago un lookup del objeto que me interesa
			IEjecutorTarea ejecutorTarea = (IEjecutorTarea) registry.lookup("EjecutorTarea");
			

			// Creo una tarea pesada
			Tarea tarea = new Tarea() {
				
				public int ejecutar() {
					Random r = new Random();
					return r.nextInt(100000) * r.nextInt(100000);
				}
			};
			
			// Le pido el resultado al ejecutor
			System.out.println("Número aleatorio es: " + ejecutorTarea.ejecutar(tarea));
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
