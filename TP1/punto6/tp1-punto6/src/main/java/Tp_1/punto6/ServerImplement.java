package Tp_1.punto6;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;



public class ServerImplement implements RemoteInt{



	public int[] sumarVector(int[] v1, int[] v2) throws RemoteException {
		// TODO Auto-generated method stub
		
	    int[] v3 = new int[5];
		for (int i=0; i<v1.length; i++) {
			v3[i] = v1[i]+v2[i];
		}
		return v3;
	}

	public int[] devolverVector(int[] v1) throws RemoteException {
		// TODO Auto-generated method stub
		for (int i=0; i<v1.length; i++) {
			v1[i]=0;
		}
		return v1;
	}


	public int[] sumarVect(Vector v4, Vector v5) throws RemoteException {
		int[] v3 = new int[5];
		for (int i=0; i<v4.getV1().length; i++) {
			v3[i] = v4.getV1()[i]+v5.getV1()[i];
			v4.getV1()[i]= 0;
			v5.getV1()[i]= 0;
		}
		return v3;
	}
}
