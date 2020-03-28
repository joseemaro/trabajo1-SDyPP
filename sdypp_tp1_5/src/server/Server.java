package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import abst.IClimaManager;

public class Server {

	public static void main(String[] args) {		
		try {			
			Registry serverRmi = LocateRegistry.createRegistry(9200);			
			// Creamos un objeto que implementa la interfaz remota
			ObjetoRemoto obj = new ObjetoRemoto();
			// Exportamos el objeto al runtime RMI pa que pueda recibir llamadas remotas
			IClimaManager stub = (IClimaManager) UnicastRemoteObject.exportObject(obj, 8000);			
			// Registramos el objeto ligado a un nombre
			serverRmi.rebind("ClimaManager", stub);
			
		} catch (Exception e) {
			System.out.println("Se produjo un error en la red.");
			e.printStackTrace();
		}

	}

}
