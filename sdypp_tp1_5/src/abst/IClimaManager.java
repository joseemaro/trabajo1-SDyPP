package abst;

import java.rmi.Remote;

public interface IClimaManager extends Remote{
	
	public int getTemperatura() throws java.rmi.RemoteException;
	
}
