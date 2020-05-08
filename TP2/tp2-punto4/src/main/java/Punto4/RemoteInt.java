package Punto4;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;


//ES LA INTERFAZ QUE IMPLEMENTA EL SERVIDOR SOBEL
public interface RemoteInt extends Remote{
	
	public byte[] sobelDistribuido (byte[] image) throws IOException, InterruptedException, NotBoundException, ClassNotFoundException;
	public String proceso () throws IOException, InterruptedException;
}
