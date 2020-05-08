package Punto4.withDownWorkers;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;

public interface RemoteInt extends Remote{
	
	public byte[] sobelDistribuido (byte[] image) throws IOException, InterruptedException, NotBoundException, ClassNotFoundException;
}
