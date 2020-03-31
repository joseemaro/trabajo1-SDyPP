package Tp_1.punto6;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Vector extends UnicastRemoteObject{
	int[] v1 = new int[5];

	public Vector(int[] v1) throws RemoteException  {
		super();
		this.v1 = v1;
	}

	public int[] getV1() {
		return v1;
	}

	public void setV1(int[] v1){
		this.v1 = v1;
	}
	
}
