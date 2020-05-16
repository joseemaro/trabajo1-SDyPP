package TP2SDPP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

//Encargado de cargar las configuraciones, hacer ping a todos los Maestros que estan levantados, se conecta con el ultimo que esta disponible, crea el hilo para el cliente y servidor .
public class Extremo {
	private final String CONF_PATH = "./config.txt";
	private final Logger log = LoggerFactory.getLogger(Configuraciones.class);
	String peerIp;
	int peerPort;
	String directory;
	
	PeerCliente peerCliente;
	PeerServidor peerServidor;
	
	Socket connMaestro;
	String masterIp;
	int masterPort;
	
	ArrayList<Archivo> liArchivos;
	
	
	public Extremo (String ip,int port,String directory) {
		//STEP 0: Configuro mis parametros
		try {
			this.peerIp = ip;
			this.peerPort = port;
			this.directory = directory;
			MDC.put("log.name", Extremo.class.getSimpleName().toString()+"-"+this.peerPort);
			loadConfig();
			//STEP 1: Cargo mis archivos disponibles
			this.liArchivos = getArchivos(directory);
			//STEP 2: Creo una nueva conexion socket 
			log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: Conectando a:"+masterIp+":"+masterPort);
			this.connMaestro = new Socket(masterIp,masterPort);
			//STEP 3: Creo mis Threads cliente y servidor
			this.peerServidor = new PeerServidor(this.peerIp,this.peerPort, this.liArchivos, directory);
			this.peerCliente = new PeerCliente(this.connMaestro,this.peerPort, this.liArchivos, directory);
			Thread tSv = new Thread(this.peerServidor);
			Thread tCli = new Thread(this.peerCliente);
			//STEP 4: Lanzo los Threads cliente y servidor
			tSv.start();
			tCli.start();
			try {
				tCli.join();
			}catch (InterruptedException  e) {
				log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: Se cerro el cliente.");
			}
		} catch (IOException e) {
			log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: No hay servidores disponibles.");
			log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: Reintentando en 10 segundos.");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: Intentando...");
			main(port, directory);
		}
	}
	
	private void loadConfig() {
		File tempFile = new File(this.CONF_PATH);
		ArrayList<String> primaryMaster = new ArrayList<String>();
		if (tempFile.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(this.CONF_PATH))) {
				String s;
				String ip;
				int port;
				while ((s = br.readLine()) != null) {
					primaryMaster.add(s);
				}
				for(String str : primaryMaster) {
					String[] strSplit = str.split(":");
					ip = strSplit[0];
					port = Integer.valueOf(strSplit[1]);
					if (available(ip, port)) {
						this.masterIp = ip;
						this.masterPort = port;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: El archivo de inicializacion no existe.");
		}		
	}
	
	private boolean available(String ip,int port) {
	    try (Socket ignored = new Socket(ip, port)) {
			PrintWriter outputChannel = new PrintWriter (ignored.getOutputStream(), true);
			outputChannel.println("imClientPinging");
			ignored.close();
	        return true;
	    } catch (IOException ignored) {
	        return false;
	    }
	}
	
	private ArrayList<Archivo> getArchivos(String directory) throws FileNotFoundException {
		String[] res = getFiles(directory);
		ArrayList<Archivo> liArchivo = new ArrayList<Archivo>();
		if ( res != null ) {
            int size = res.length;
            for ( int i = 0; i < size; i ++ ) {            	
                if (res[i] != null) {
                	Archivo a = new Archivo(res[ i ]);
                	liArchivo.add(a);
                }
            }
        }
		return liArchivo;
	}

    public String[] getFiles( String path ) {
        String[] arr_res = null;
        File f = new File( path );
        log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: "+f.getAbsolutePath());
        if ( f.isDirectory()) {
            List<String> res   = new ArrayList<>();
            File[] arr_content = f.listFiles();
            int size = arr_content.length;
            for ( int i = 0; i < size; i ++ ) {
                if ( arr_content[ i ].isFile( ))
                res.add( arr_content[ i ].getName());
            }
            arr_res = res.toArray( new String[ 0 ] );
        } else {
        	log.info("[PEER-"+this.peerIp+":"+this.peerPort+"]: No existe la carpeta: "+path+" creandola...");
        	f.mkdir();        	
        }
        return arr_res;
    }


	public static void main( int port,String folder) {
		new Extremo("localhost",port,folder);
		MDC.remove("log.name");
	}
}
