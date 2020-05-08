package Punto4.withDownWorkers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Punto4.withDownWorkers.RemoteInt;


public class SobelClient {
	//servodpr que funciona como dispacher entre el cliente y los workers(a traves de colas).		
	private int portServer; 

	private final static Logger log = LoggerFactory.getLogger(SobelClient.class);			
	
	public SobelClient(int portServer) {
		this.portServer = portServer;
	}
	
	
	public static void main(String[] args) throws IOException, NotBoundException, InterruptedException, TimeoutException, ClassNotFoundException {

		int portServer = 1200;
		//creo cliente en puerto 1200
		SobelClient sobel1  = new SobelClient(portServer);
		log.info("----- SobelClient iniciado -----");
			
		//esta es la imagen a procesar
		String pathImage = "images/pc1.jpg";   
		boolean salir= false;
		do {
			System.out.println("BIENVENIDO AL MENU");
			System.out.println("1- Sobel a nivel local CLIENTE");
			System.out.println("2- Sobel Distribuido");				
			System.out.println("0- Finalizar Programa");
			System.out.println("Ingrese la opcion que desea realizar a continuacion=");
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			int op =  scanner.nextInt();
			//int op = 2;
			String rutaResultado;
			switch(op) {
			case 1:
				rutaResultado = sobel1.sobelCall(pathImage,"local");
				if (rutaResultado!=null) {
					System.out.println("SE REALIZO EL PROCESO SOBEL SOBRE LA IMAGEN DE FORMA CORRECTA.");
					log.info("La nueva imagen se ubica dentro del proyecto en la ruta: " + rutaResultado);
					salir=true;
				}
				break;
			case 2:
				rutaResultado = sobel1.sobelCall(pathImage,"distribuido");
				if (rutaResultado!=null) {
					System.out.println("SE REALIZO EL PROCESO SOBEL SOBRE LA IMAGEN DE FORMA CORRECTA.");
					log.info("La nueva imagen se ubica dentro del proyecto en la ruta: " + rutaResultado);
					salir=true;
				}
				break;
			case 0:
				System.out.println("------- Programa finalizado -------");
				
				salir = true;
				break;		
			default:
				System.out.println("La opcion ingresada no corresponde.");
				System.out.println("");
			}
		}while (!salir);
	}
	
	//aca se llama a realizar el proceso sobel ya sea local o distribuido
	public String sobelCall(String pathImage, String tipoSobel) throws IOException, NotBoundException, InterruptedException, TimeoutException, ClassNotFoundException {		
		//mido tiempo de inicio 
		long start = System.currentTimeMillis(); 
		
		String rutaEntrada = pathImage;
		//obtenga tipo de la imagen
		int pos = rutaEntrada.indexOf(".");
		String nameImage = rutaEntrada.substring(0, pos);
		String extensionImage = rutaEntrada.substring(pos, rutaEntrada.length());;	
		//creo nombre de la imagen resultante
		String rutaSalida= nameImage + "-Sobel"+ extensionImage;
		@SuppressWarnings("unused")
		String rutaParcialSalida= "";
		
		//leo iamgen entrante
		FileInputStream inFile = new FileInputStream(rutaEntrada);
		BufferedImage imgEntrante = ImageIO.read(inFile);
		BufferedImage resultImg;
		
		//llamo a sobel local
		if (tipoSobel.equals("local")) {
			resultImg = sobelLocal(imgEntrante);
		}else {
			//llamo a sobel distribuido
			Registry clientRMI = LocateRegistry.getRegistry("localhost", this.portServer);
			System.out.println("-x-x-x- Cliente conectado a server en puerto "+this.portServer+"-x-x-x");
			RemoteInt ri = (RemoteInt) clientRMI.lookup("sobelImagenes");
			ByteArrayOutputStream imgSend = new ByteArrayOutputStream();
			//escribo la iamgen a enviar
			ImageIO.write(imgEntrante, "jpg", imgSend);
			//llamo al proceso distribuido de sobel
			byte[] imgSalida = ri.sobelDistribuido(imgSend.toByteArray());
			//leo resultado recibido
			resultImg = ImageIO.read(new ByteArrayInputStream(imgSalida));
		}
		
		FileOutputStream outResultFile = new FileOutputStream(rutaSalida);
		//escribo resultado en la imagen
		ImageIO.write(resultImg, "JPG", outResultFile);
		//tomo tiempo salida
		long end = System.currentTimeMillis();
		log.info(" ELAPSED TIME: "+(end-start));
		return rutaSalida;
	}			
			

	public BufferedImage sobelLocal(BufferedImage inImg) throws InterruptedException {
	    int[][] sobel_x = new int[][]{
			{-1, 0,1}, 
			{-2, 0,2}, 
			{-1, 0,1}	
		};
		
		int[][] sobel_y = new int[][]{
			{-1,-2,-1},
			{0, 0, 0},
			{1, 2, 1}				
		};
		
		int i, j;
		float[][] Gx;
		float[][] Gy;
		float[][] G;
		
		int width = inImg.getWidth();
		int height = inImg.getHeight();
		log.info("W:" + width);
		log.info("H:" + height);
		float[] pixels = new float[(int) width * (int) height];
		float[][] output = new float[(int) width][(int) height];
		
			int counter2 = 0;
	        for (int xx = 0; xx < width; xx++) {

	            for (int yy = 0; yy < height; yy++) {
	            	try{
	            		
	            		pixels[counter2] = inImg.getRGB(xx, yy);
	            		output[xx][yy] = inImg.getRGB(xx, yy);
	            	
	            	}catch(Exception e1){
	            	    System.out.println("Error: " + " x: "+ yy + " y: " + xx);
	            	    
	            	}
	                counter2++;
	            }
	        }
	        

		Gx = new float[width][height];
		Gy = new float[width][height];
		G = new float[width][height];

		for (i = 0; i < width; i++) {
			for (j = 0; j < height; j++) {
				if (i == 0 || i == width - 1 || j == 0 || j == height - 1)
					Gx[i][j] = Gy[i][j] = G[i][j] = 0;
				else {
					// Calculo x			    	  
			    	Gx[i][j] = ((sobel_x[0][0] * output[i-1][j-1]) + (sobel_x[0][1] * output[i][j-1]) + (sobel_x[0][2] * output[i+1][j-1]) + (sobel_x[1][0] * output[i-1][j]) + (sobel_x[1][1] * output[i][j]) + (sobel_x[1][2] * output[i+1][j]) + (sobel_x[2][0] * output[i-1][j+1]) + (sobel_x[2][1] * output[i][j+1]) + (sobel_x[2][2] * output[i+1][j+1])); 
			    	// Calculo y
			    	Gy[i][j] = ((sobel_y[0][0] * output[i-1][j-1]) + (sobel_y[0][1] * output[i][j-1]) + (sobel_y[0][2] * output[i+1][j-1]) + (sobel_y[1][0] * output[i-1][j]) + (sobel_y[1][1] * output[i][j]) + (sobel_y[1][2] * output[i+1][j]) + (sobel_y[2][0] * output[i-1][j+1]) + (sobel_y[2][1] * output[i][j+1]) + (sobel_y[2][2] * output[i+1][j+1]));
					
					G[i][j] = (Math.abs(Gx[i][j]) + Math.abs(Gy[i][j]));					
				}
			}
		}
		
		// obtengo max y min para nivel de detalle
		int counter1 = 0;
		for (int yy = 0; yy < height; yy++) {
			for (int xx = 0; xx < width; xx++) {
					pixels[counter1] = (int) G[xx][yy];
					counter1 = counter1 + 1;
			}
		}
		
		BufferedImage imgSalida = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		imgSalida.getRaster().setPixels(0, 0, width, height, pixels);
		return imgSalida;
	}
	
}
