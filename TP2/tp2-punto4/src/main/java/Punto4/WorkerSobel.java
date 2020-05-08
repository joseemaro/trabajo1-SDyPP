package Punto4;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;

public class WorkerSobel {
	//matriz x
    static int[][] sobel_x = new int[][]{
		{-1, 0,1}, 
		{-2, 0,2}, 
		{-1, 0,1}	
	};
	//matriz y
	static int[][] sobel_y = new int[][]{
		{-1,-2,-1},
		{0, 0, 0},
		{1, 2, 1}				
	};
	//crea log
	private final static Logger log = LoggerFactory.getLogger(WorkerSobel.class);
	private int idWorker; //id
	private Channel queueChannel; //canal
	private String queueName;  //cola
	
	public WorkerSobel(int idWorker, Channel queueChannel) throws IOException {
	
		log.info("-X-X-X-X-X- WORKER SOBEL INICIADO -X-X-X-X-X-X");
		this.idWorker = idWorker;
		this.queueChannel = queueChannel;
		startQueue();
	}

	public void startQueue() throws IOException {
		
		this.queueName = "QUEUE_W" + this.getIdWorker();  //NOMBRE DE COLA COMPUESO POR STRING Y ID WORKER.
		this.queueChannel.queueDeclare(this.queueName, true, false, false, null);
		this.queueChannel.basicConsume(this.queueName, true, receivedInParcialImage, consumerTag -> {});
		System.out.println("----- El worker sobel creo la cola =  "+this.queueName +"-----");
	}

	
	public void deleteQueue() {	}
	
	
	private DeliverCallback receivedInParcialImage = (consumerTag, delivery) -> {
		try {
			Gson googleJson = new Gson();
			SobelRequest request = googleJson.fromJson(new String(delivery.getBody(),"UTF-8"), SobelRequest.class);
			log.info("El worker Sobel numero =  " +this.idWorker +"  ha leido un mensaje de su cola.");
			//realiza el sobel y recibe la respuesta en un string de bytes
			byte[] imgParcialSalida = sobel(request.getInParcialImg());
			request.setOutImg(imgParcialSalida);
			
			String sResponse = googleJson.toJson(request);
			this.queueChannel.basicPublish("", "QUEUE_SOBEL_OUT", MessageProperties.PERSISTENT_TEXT_PLAIN,sResponse.getBytes("UTF-8") );
			log.info("El worker Sobel numero =  " +this.idWorker +" ha enviado su respuesta a QUEUE_SOBEL_OUT.");
		    
		} catch (ClassNotFoundException | InterruptedException | NotBoundException e) {e.printStackTrace();}
	};
	
	
	public int getIdWorker() {return idWorker;}
	public String getQueueName() {return queueName;}
	
	//la funcion sobel en si
	public byte[] sobel(byte[] inImgBytes) throws IOException, InterruptedException, NotBoundException, ClassNotFoundException {
		BufferedImage inImg = ImageIO.read(new ByteArrayInputStream(inImgBytes));
		int i, j;
		float[][] Gx;
		float[][] Gy;
		float[][] G;
		
		int width = inImg.getWidth();
		int height = inImg.getHeight();
		//System.out.println("W:" + width);
		//System.out.println("H:" + height);
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
					//Calcula el eje x
			    	Gx[i][j] = ((sobel_x[0][0] * output[i-1][j-1]) + (sobel_x[0][1] * output[i][j-1]) + (sobel_x[0][2] * output[i+1][j-1]) + (sobel_x[1][0] * output[i-1][j]) + (sobel_x[1][1] * output[i][j]) + (sobel_x[1][2] * output[i+1][j]) + (sobel_x[2][0] * output[i-1][j+1]) + (sobel_x[2][1] * output[i][j+1]) + (sobel_x[2][2] * output[i+1][j+1])); 	  
			    	// Calcula el eje y
			    	Gy[i][j] = ((sobel_y[0][0] * output[i-1][j-1]) + (sobel_y[0][1] * output[i][j-1]) + (sobel_y[0][2] * output[i+1][j-1]) + (sobel_y[1][0] * output[i-1][j]) + (sobel_y[1][1] * output[i][j]) + (sobel_y[1][2] * output[i+1][j]) + (sobel_y[2][0] * output[i-1][j+1]) + (sobel_y[2][1] * output[i][j+1]) + (sobel_y[2][2] * output[i+1][j+1]));
			    	
			    	G[i][j] = (Math.abs(Gx[i][j]) + Math.abs(Gy[i][j]));
					
				}
			}
		}
		
		//se van a obtener min y max para poder realizar divisiones y nivel de detalle.
		int contador = 0;
		for (int yy = 0; yy < height; yy++) {
			for (int xx = 0; xx < width; xx++) {
				pixels[contador] = (int) G[xx][yy];
				contador = contador + 1;
			}
		}
		
		BufferedImage imgSalida = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		imgSalida.getRaster().setPixels(0, 0, width, height, pixels);

		ByteArrayOutputStream imgResultante = new ByteArrayOutputStream();
		ImageIO.write(imgSalida, "jpg", imgResultante);
		//envia string de bytes al servidor
		return imgResultante.toByteArray();
	}

}