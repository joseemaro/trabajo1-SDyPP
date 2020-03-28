package Tp_1.punto6;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInt extends Remote{
	public int[] sumarVector(int[] v1, int[] v2) throws RemoteException;
	public int[] devolverVector(int[] v1) throws RemoteException;

}
