package SDyPP.SDyPP_tp1_7;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IEjecutorTarea extends Remote{

	public int ejecutar(Tarea t) throws RemoteException;
	
}
