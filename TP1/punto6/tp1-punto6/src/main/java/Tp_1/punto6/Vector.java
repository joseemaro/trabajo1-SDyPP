package Tp_1.punto6;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Vector implements Serializable{
	int[] v1 = new int[5];

	public Vector(int[] v1)  {
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
