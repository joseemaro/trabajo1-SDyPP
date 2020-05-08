package Punto4.withDownWorkers;
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
	//eje x
    static int[][] sobel_x = new int[][]{
		{-1, 0,1}, 
		{-2, 0,2}, 
		{-1, 0,1}	
	};
	//eje y
	static int[][] sobel_y = new int[][]{
		{-1,-2,-1},
		{0, 0, 0},
		{1, 2, 1}				
	};
	
	//creo log
	private final static Logger log = LoggerFactory.getLogger(WorkerSobel.class);
	private int idWorker;
	private Channel queueChannel;
	private String queueName;
	
	//constructor worker
	public WorkerSobel(int idWorker, Channel queueChannel) throws IOException {
		
		log.info("-X-X-X-X-X-X- WORKER INICIADO -X-X-X-X-X-");
		this.idWorker = idWorker;
		this.queueChannel = queueChannel;
		//llamo a definir la cola y crearla
		startQueue();
	}

	//INICIAR COLA
	public void startQueue() throws IOException {
		this.queueName = "QUEUE_W" + this.getIdWorker();
		this.queueChannel.queueDeclare(this.queueName, true, false, false, null);
		this.queueChannel.basicConsume(this.queueName, true, receivedInParcialImage, consumerTag -> {});
		log.info("----- WorkerSobel "+this.idWorker +" creo la cola "+this.queueName +"-----");
	}

	
	//public void deleteQueue() {	}
	
	
	private DeliverCallback receivedInParcialImage = (consumerTag, delivery) -> {
		try {
			Gson googleJson = new Gson();
			//el worker lee de la cola un mensaje
			SobelRequest request = googleJson.fromJson(new String(delivery.getBody(),"UTF-8"), SobelRequest.class);
			log.info("WorkerSobel " +this.idWorker +" leyo un mensaje de su cola.");
			
			//creo un string de bytes a patir de lo recibido
			byte[] imgParcialSalida = sobel(request.getInParcialImg());
			//establece imagen resuelta parcial
			request.setOutImg(imgParcialSalida);
			//setea un estado para esa peticion de solucion
			request.setState(StateSobelRequest.DONE);

			String sResponse = googleJson.toJson(request);
			//envia su respuesta
			this.queueChannel.basicPublish("", "QUEUE_SOBEL_OUT", MessageProperties.PERSISTENT_TEXT_PLAIN,sResponse.getBytes("UTF-8") );
			log.info("WorkerSobel " +this.idWorker +" envio su respuesta a QUEUE_SOBEL_OUT.");
		    
		} catch (ClassNotFoundException | InterruptedException | NotBoundException e) {e.printStackTrace();}
	};
	
	
	public int getIdWorker() {return idWorker;}
	public String getQueueName() {return queueName;}
	
	
	//proceso sobel
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
					// Calculo x			    	  
			    	Gx[i][j] = ((sobel_x[0][0] * output[i-1][j-1]) + (sobel_x[0][1] * output[i][j-1]) + (sobel_x[0][2] * output[i+1][j-1]) + (sobel_x[1][0] * output[i-1][j]) + (sobel_x[1][1] * output[i][j]) + (sobel_x[1][2] * output[i+1][j]) + (sobel_x[2][0] * output[i-1][j+1]) + (sobel_x[2][1] * output[i][j+1]) + (sobel_x[2][2] * output[i+1][j+1])); 
			    	  
			    	// Calculo y
			    	Gy[i][j] = ((sobel_y[0][0] * output[i-1][j-1]) + (sobel_y[0][1] * output[i][j-1]) + (sobel_y[0][2] * output[i+1][j-1]) + (sobel_y[1][0] * output[i-1][j]) + (sobel_y[1][1] * output[i][j]) + (sobel_y[1][2] * output[i+1][j]) + (sobel_y[2][0] * output[i-1][j+1]) + (sobel_y[2][1] * output[i][j+1]) + (sobel_y[2][2] * output[i+1][j+1]));
					
					G[i][j] = (Math.abs(Gx[i][j]) + Math.abs(Gy[i][j]));					
				}
			}
		}
		
		// obtengo los max y min para resolver el nivel de detalle
		int counter1 = 0;
		for (int yy = 0; yy < height; yy++) {
			for (int xx = 0; xx < width; xx++) {
				pixels[counter1] = (int) G[xx][yy];
				counter1 = counter1 + 1;
			}
		}
		
		BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		outImg.getRaster().setPixels(0, 0, width, height, pixels);

		ByteArrayOutputStream imgResult = new ByteArrayOutputStream();
		//escribo la imagen parcial resultante del proceso
		ImageIO.write(outImg, "jpg", imgResult);
		
		
		//int ms = (int)(Math.random()*10000);
		//Thread.sleep(ms);
		//System.out.println("Worker "+this.getIdWorker()+" durmio "+ms+" milisegundos");
		//devuelvo lo imagen en un array de bytes
		return imgResult.toByteArray();
	}



}