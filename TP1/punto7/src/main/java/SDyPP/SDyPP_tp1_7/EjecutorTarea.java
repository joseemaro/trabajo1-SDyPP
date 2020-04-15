package SDyPP.SDyPP_tp1_7;

import java.rmi.RemoteException;

public class EjecutorTarea implements IEjecutorTarea {

	public int ejecutar(Tarea t) throws RemoteException {
		return t.ejecutar();
	}

}
