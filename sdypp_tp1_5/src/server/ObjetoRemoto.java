package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import abst.IClimaManager;

public class ObjetoRemoto implements IClimaManager{

	@Override
	public int getTemperatura() throws RemoteException {
		return new Random().nextInt(30);
	}

}
