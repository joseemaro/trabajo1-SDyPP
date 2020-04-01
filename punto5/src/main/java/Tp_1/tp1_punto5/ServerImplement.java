package Tp_1.tp1_punto5;

import java.rmi.RemoteException;

public class ServerImplement implements RemoteInt{

	public String devolverClima(String ciudad) throws RemoteException {
		// TODO Auto-generated method stub
		double temperatura = (Math.random()*40);
		String msg ="la temperatura en la ciudad de " + (String) ciudad + "es de " + temperatura + " grados.";
		return msg ;
		
	}

}
