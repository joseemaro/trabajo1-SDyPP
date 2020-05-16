package TP2SDPP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

//Lee por socket el pedido del cliente(buscar, descargar), lee el archivo en el file System y crea el object Archivo, lo envia y devuelve un mensaje de error en ek caso que esto ocurra.
public class Trabajador implements Runnable {
	ArrayList<Archivo> liArchivos;
	Socket client;
	String directory;
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	private final Logger log = LoggerFactory.getLogger(Trabajador.class);
	
	public Trabajador(ArrayList<Archivo> liArchivos, Socket client,String directory) {
		this.client = client;
		this.liArchivos = liArchivos;
		this.directory = directory;
	}
	
	private byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();        
        }catch(IOException e){
            e.printStackTrace();
        }
        return bArray;
    }
	
	public void enviarArchivo(String name) {
        try {
			Archivo toSend = null;
			File file;
			int index = lookupArchivo(name);
			if (index >= 0) {
				file = new File(directory+"/"+name);
				if (file.exists()) {
					byte[] bArray = readFileToByteArray(file);
					toSend = new Archivo(name,bArray);		
			        outputChannel.println("sending");
			        outputChannel.flush();
					ObjectOutputStream os = new ObjectOutputStream(this.client.getOutputStream());
					log.info("[PEER-SERVER-WORKER-"+this.client.getLocalPort()+"]: Worker enviando el archivo: "+name);	
					os.writeObject(toSend);	       
				}else {
			        outputChannel.println("error");
			        log.info("[PEER-SERVER-WORKER-"+this.client.getLocalPort()+"]: Archivo "+name+" no existe");
				}
			}else {
				outputChannel.println("error");
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public int lookupArchivo(String name) {
		for (Archivo archivo : liArchivos) {
			if(archivo.getName().contains(name)) {
				return liArchivos.indexOf(archivo);
			}
		}
		return -1;
	}
	
	public ArrayList<String> lookupMultipleArchivo(String name) {
		ArrayList<String> resultado = new ArrayList<String>();
		int counter = 0;
		for (Archivo archivo : liArchivos) {
			counter ++;
			if(archivo.getName().contains(name)) {
				resultado.add(counter+"."+archivo.getName());
			}
		}
		return resultado;
	}
	
	
	
	@Override
	public void run() {
		String msg;
		String[] msgParced;
		MDC.put("log.name", Trabajador.class.getSimpleName().toString()+"-"+this.client.getLocalPort()+"-"+Thread.currentThread().getId());
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
			msg = this.inputChannel.readLine();
			log.info("[PEER-SERVER-WORKER-"+this.client.getLocalPort()+"]: Recibi: "+msg);
			msgParced = msg.split("=");
			switch (msgParced[0]) {
			case "buscar":
				ArrayList<String> busqueda = lookupMultipleArchivo(msgParced[1]);
				if (!(busqueda == null)) {
					for(String m : busqueda)
						this.outputChannel.println(m);
				}
				break;
			case "descargar":
				enviarArchivo(msgParced[1]);
				log.info("[PEER-SERVER-WORKER-"+this.client.getLocalPort()+"]: Archivo enviado!");
				break;
			default:
				break;
			}
			this.outputChannel.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
