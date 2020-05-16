package TP2SDPP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuraciones {
	String archivo;
	private final Logger log = LoggerFactory.getLogger(Configuraciones.class);
	
	public Configuraciones (String path) {
		this.archivo = path;
	}
	
	
	public ArrayList<String[]> doConfig(String masterIp,int masterPort){
		log.info("Configurando al Marstro " + masterIp + ":"  + masterPort + " " + archivo);
		ArrayList<String[]> rta = new ArrayList<String[]>();
		File tempFile = new File(this.archivo);
		FileWriter fw;
		String sNodo = masterIp +":" + masterPort;
		if (tempFile.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(this.archivo))) {
				String str;
				boolean exist = false;
				while ((str = br.readLine()) != null) {
					if(str.contentEquals(sNodo)) {
						log.info("El nodo Maestro ya esta registrado en las configuraciones.");
						exist = true;
					}
					if(!str.contentEquals(sNodo) && !str.contentEquals("")) {
						log.info("Encontre otro nodo maestro "+ str);
						rta.add(str.split(":"));
					}
				}
				if (!exist) {
					log.info("Registrando el nodo en configuraciones.");
					fw = new FileWriter(tempFile, true);
					fw.write(sNodo+"\r\n");
					fw.close();
				}
				return rta;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			try {
				log.info("El archivo de mensajes no existe...\nCreandolo en "+ tempFile.getCanonicalPath());
				tempFile.createNewFile();
				log.info("Registrando el nodo en configuraciones.");
				fw = new FileWriter(tempFile, true);
				fw.write(sNodo+"\r\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rta;
	}
}
