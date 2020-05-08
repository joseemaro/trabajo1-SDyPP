package Punto4;

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
import Punto4.RemoteInt;


public class SobelClient {
				
	private int portServer; //puerto del servidor que relaciona los workers
				
	
	public SobelClient(int portServer) {
		this.portServer = portServer;
	}
	
	
	public static void main(String[] args) throws IOException, NotBoundException, InterruptedException, TimeoutException, ClassNotFoundException {

		int portServer = 1200;
		SobelClient sobel1  = new SobelClient(portServer);
		System.out.println("----- SobelClient iniciado -----");

		//aca esta almacenada la imagen que se va a transformar
		String pathImage = "images/pc5.jpg";   
		
		boolean salir= false;
		do {
			//pido las opciones
			System.out.println("BIENVENIDO AL MENU");
			System.out.println("1- Sobel a nivel local CLIENTE");
			System.out.println("2- Sobel Distribuido");				
			System.out.println("0- Finalizar Programa");
			System.out.println("Ingrese la opcion que desea realizar a continuacion=");
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			int opcion =  scanner.nextInt();
			String rutaResultado;
			switch(opcion) {
			case 1:
				
				//se llama a sobel local
				rutaResultado = sobel1.sobelCall(pathImage,"local");
				if (rutaResultado!=null) {
					System.out.println("SE REALIZO EL PROCESO SOBEL SOBRE LA IMAGEN DE FORMA CORRECTA.");
					System.out.println("La nueva imagen se ubica dentro del proyecto en la ruta: " + rutaResultado);
					salir=true;
				}
				break;
			case 2:
				
				//se llama a sobel distribuido
				rutaResultado = sobel1.sobelCall(pathImage,"distribuido");
				if (rutaResultado!=null) {
					System.out.println(" ");
					System.out.println("SE REALIZO EL PROCESO SOBEL SOBRE LA IMAGEN DE FORMA CORRECTA.");
					System.out.println("La nueva imagen se ubica dentro del proyecto en la ruta: " + rutaResultado);
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
	
	
	public String sobelCall(String pathImage, String tipoSobel) throws IOException, NotBoundException, InterruptedException, TimeoutException, ClassNotFoundException {		

		long start = System.currentTimeMillis(); //toma el tiempo inicial de la tarea
		String entryPath = pathImage;
		int pos = entryPath.indexOf(".");
		String nameImage = entryPath.substring(0, pos);
		String extensionImage = entryPath.substring(pos, entryPath.length());;		
		String outputPath= nameImage + "-Sobel"+ extensionImage; //defino nombre y extension de la imagen que crea
		@SuppressWarnings("unused")
		String outputParcialPath= "";
		
		FileInputStream inFile = new FileInputStream(entryPath);
		BufferedImage imgEntrada = ImageIO.read(inFile);
		BufferedImage imgResultado;
		//si se realiza sobel local
		if (tipoSobel.equals("local")) {
			imgResultado = sobelLocal(imgEntrada);
		}else { //si se realiza sobel distribuido
			//conecto al cliente por rmi
			Registry clientRMI = LocateRegistry.getRegistry("localhost", this.portServer);
			System.out.println("-X-X-X-X- Cliente conectado a server principal en puerto "+this.portServer+"-X-X-X-X-");
			RemoteInt ri = (RemoteInt) clientRMI.lookup("sobelImagenes");
			
			//creo el array de bytes para la imagen
			ByteArrayOutputStream imgEnvio = new ByteArrayOutputStream();
			//le paso la imagen entrante, tipo y array vacio
			ImageIO.write(imgEntrada, "jpg", imgEnvio);
			//se llama al sobel distribuido definido en la interfaz y le paso el string de bytes
			byte[] imgSalida = ri.sobelDistribuido(imgEnvio.toByteArray());
			imgResultado = ImageIO.read(new ByteArrayInputStream(imgSalida));
		}
		//leo imagen y creo el resultado
		FileOutputStream outResultFile = new FileOutputStream(outputPath);
		ImageIO.write(imgResultado, "JPG", outResultFile);
		
		//finaliza el tiempo de proceso
		long endTS = System.currentTimeMillis();
		System.out.println(" ELAPSED TIME: "+(endTS-start));
		
		//devuelve path
		return outputPath;
	}			
			
	// -----------------------------------------------------------------------------------------------------
	public BufferedImage sobelLocal(BufferedImage inImg) {
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
		System.out.println("W:" + width);
		System.out.println("H:" + height);
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
		
		// obtengo minimo y maximo para las divisiones posteriores
		int contador = 0;
		for (int yy = 0; yy < height; yy++) {
			for (int xx = 0; xx < width; xx++) {
				pixels[contador] = (int) G[xx][yy];
				contador = contador + 1;
			}
		}
		
		BufferedImage imgSalida = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		imgSalida.getRaster().setPixels(0, 0, width, height, pixels);

		return imgSalida;
	}
}
