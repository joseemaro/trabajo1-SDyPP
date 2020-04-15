package Tp_1.tp1_punto5;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInt extends Remote{
	public String devolverClima(String ciudad) throws RemoteException;
}
