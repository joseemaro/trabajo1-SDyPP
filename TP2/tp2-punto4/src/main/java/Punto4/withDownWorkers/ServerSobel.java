package Punto4.withDownWorkers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;

public class ServerSobel implements RemoteInt {

	private int port;
	private final static Logger log = LoggerFactory.getLogger(ServerSobel.class);
	private Map<Integer, String> colasActivasWorkers; // <id worker,nombre de la cola> 
	private ArrayList<WorkerSobel> workers;
	private ArrayList<SobelRequest> workerRequests;
	//private ConnectionFactory connectionFactory;
	//private Connection queueConnection;
	private Channel queueChannel;
    private String nombreColaSalida;
	private static final long maxWaitTime = 3000; // 3 segundos. 
	private static final long periodWaitTime = 200; // 0.2 segundos. 
	private static final int sizeBorderSobel = 1;
    
	public ServerSobel(String ip, int port, ArrayList<WorkerSobel> workers, Channel queueChannel) throws NotBoundException, IOException, InterruptedException {
		this.port = port;
		this.workers= workers;
		this.colasActivasWorkers = new HashMap<Integer,String>();
		this.workerRequests = new ArrayList<SobelRequest>();
		this.nombreColaSalida = "QUEUE_SOBEL_OUT";
		this.queueChannel = queueChannel;
		//inicio cliente sobel
		this.startSobelRMIforClient();
		//inicio rabbit
		configureConnectionToRabbit();
		//inicio colas workers
		this.startWorkersQueues();
	}
	
	
	 //-----------------------------------------------------------------------------------------------------------------------//
		public static void main(String[] args) throws IOException, NotBoundException, InterruptedException, TimeoutException {
			Logger log = LoggerFactory.getLogger(ServerSobel.class);
			log.info("-x-x-x-x- Server Dispatcher iniciado -x-x-x-x-x-");
			System.out.println("Ingrese cantidad de servidores [fijos]>");
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			int qWorkers=  scanner.nextInt();
			int portInicialWorker = 800;
			ArrayList<WorkerSobel> workerList = new ArrayList<WorkerSobel>();
		
			ConnectionFactory connectionFactory = new ConnectionFactory();
			connectionFactory.setHost("localhost");
			Connection queueConnection = connectionFactory.newConnection();
			Channel queueChannel = queueConnection.createChannel();
			
			for (int i =0 ; i<qWorkers; i++) {	
				@SuppressWarnings("unused")
				int portW = portInicialWorker + i;
				WorkerSobel ws = new WorkerSobel(i, queueChannel);
				workerList.add(ws);
				//Thread tW = new Thread(ws);
				//tW.start();
			}
			
			System.out.println("");
			int portRMIserver = 1200;
			@SuppressWarnings("unused")
			ServerSobel ss = new ServerSobel("localhost",portRMIserver,workerList, queueChannel);
						
		}
	
	private void configureConnectionToRabbit() {
		try {

			this.queueChannel.queueDeclare(this.nombreColaSalida, true, false, false, null);
			this.queueChannel.basicConsume(this.nombreColaSalida, true, receivedOutImages, consumerTag -> {});
			
		} catch (IOException e) {e.printStackTrace();
		}	
	}

	private DeliverCallback receivedOutImages = (consumerTag, delivery) -> {
        Gson googleJson = new Gson();
		//recibe la imagen
        SobelRequest response = googleJson.fromJson(new String(delivery.getBody(),"UTF-8"), SobelRequest.class);
        log.info("ServerSobel recibio una imagen del worker "+ response.getIdWorker());
    	
		boolean find = false;
        int i =0; int pos = -1;
        // la encuentro en mi copia local de Requests para actualizarle estado y OutImg.
        while ((i<this.workerRequests.size())&&(!find)) {
        	if (this.workerRequests.get(i).getIdWorker()==response.getIdWorker()) {
        		find = true;
        		pos = i;
        	}
        	i++;
        }
        
        if (find) {
    	    BufferedImage imgSalida = ImageIO.read(new ByteArrayInputStream(response.getOutImg()));// imagen resultado parcial CON LOS BORDES
        	int w = imgSalida.getWidth();
    		@SuppressWarnings("unused")
			int h = imgSalida.getHeight();
    		@SuppressWarnings("static-access")
			BufferedImage subImagen = imgSalida.getSubimage(this.sizeBorderSobel, imgSalida.getMinY(),w-(this.sizeBorderSobel*2) , imgSalida.getHeight());
    	    
			ByteArrayOutputStream imgParcialSalida = new ByteArrayOutputStream();
			ImageIO.write(subImagen, "jpg", imgParcialSalida);
			
        	this.workerRequests.get(pos).setOutImg(imgParcialSalida.toByteArray());
        	this.workerRequests.get(pos).setState(response.getState());
        }
    };
    
    
	private void startSobelRMIforClient() throws RemoteException {
		
		Registry server = LocateRegistry.createRegistry(this.port);
		log.info("-----ServerSobel Servicio RMI Iniciado en puerto "+this.port+"-----");
		
		RemoteInt serviceSobel = (RemoteInt) UnicastRemoteObject.exportObject(this, 8000);
		log.info("-----ServerSobel asociado a puerto "+8000+" -----");
		
		server.rebind("sobelImagenes", serviceSobel);
		log.info("-----Server bind de servicio JNDI realizado -----");
		
		log.info("-----conexion para cliente RMI finalizada correctamente.");
		System.out.println("");

	}

	
	public void startWorkersQueues() throws NotBoundException, IOException, InterruptedException {
		for (int i =0 ; i<this.workers.size(); i++) {	
			colasActivasWorkers.put(this.workers.get(i).getIdWorker(),this.workers.get(i).getQueueName() );
		}

		log.info("-- ServerSobel Instancio las colas activas de todos sus workers");
	}
	
	
	@SuppressWarnings("static-access")
	public byte[] sobelDistribuido(byte[] image) throws IOException, InterruptedException, NotBoundException, ClassNotFoundException {
		log.info("--INGRESO A PROCESO 'SOBELDISTRIBUIDO' ENTRE CLIENTE Y SERVERSOBEL  --");
		
		BufferedImage imgEntrante = ImageIO.read(new ByteArrayInputStream(image));// imagen que recibo de SobelClient
		BufferedImage imgSaliente; //imagen final que paso a SobelClient
		BufferedImage imgParcialWorker; //Parte de la imagen que paso al Worker
		@SuppressWarnings("unused")
		BufferedImage imgParcialRecibida;// Parte de la imagen que recibo del Worker	
		int qSector = (int)(imgEntrante.getWidth()/this.workers.size());
		System.out.println("tamaño del sector> "+qSector);
		int currentSector = 0;
		int currentWorker = 0;
	
		// declaro variables para imagen resultado
		int w = imgEntrante.getWidth(); 
		int h = imgEntrante.getHeight();
		imgSaliente = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		
		int qPart= 1;
		while (qPart <= this.workers.size() ) {// una parte de la imagen original por cada worker
			int proxSectorSize = currentSector+(2*qSector);
			int borderIzq=0;
			// si no es el sector inicial entonces envia una imagen mas grante para que el borde sobel no recorte parte de la imagen original
			if (currentSector!=0) {
				borderIzq = this.sizeBorderSobel;
			}
			if (proxSectorSize>imgEntrante.getWidth()) {// cuando la division en partes entre los workers no da exacta, se asignara el resto al ultimo Worker
				int proxSector = currentSector+qSector;
			    int untilImageEnd = qSector+(imgEntrante.getWidth() - proxSector);
			    imgParcialWorker = imgEntrante.getSubimage(currentSector-borderIzq, imgEntrante.getMinY(), untilImageEnd+(this.sizeBorderSobel), imgEntrante.getHeight()); //funcion para partir la imagen.			
			}else {// caso normal
				imgParcialWorker = imgEntrante.getSubimage(currentSector-borderIzq, imgEntrante.getMinY(), qSector+(this.sizeBorderSobel*2), imgEntrante.getHeight()); 
			}
			
			ByteArrayOutputStream inImgParam = new ByteArrayOutputStream();
			ImageIO.write(imgParcialWorker, "jpg", inImgParam);
			//llamo a ejecutar sobel en el worker 
			SobelRequest request = new SobelRequest(this.workers.get(currentWorker).getIdWorker(), inImgParam.toByteArray());
			sendImgToWorker(request);
			this.workerRequests.add(request);
			qPart++;
			currentSector+= qSector;
			currentWorker++;
			if (currentWorker==this.workers.size()) {
				currentWorker = 0;
			}
		}
		//espera respuestas
		waitingImagesSobel();
		//une respuestas
		imgSaliente = joinImages(imgEntrante);
		
		//escribo imagen final
		ByteArrayOutputStream imgResult = new ByteArrayOutputStream();
		ImageIO.write(imgSaliente, "jpg", imgResult);
		log.info("Resultado final de Sobel enviado al cliente (via RMI)");
		return imgResult.toByteArray();
	}
	
	
	public void sendImgToWorker(SobelRequest request) throws UnsupportedEncodingException, IOException {
		//envia a Worker 
		Gson googleJson = new Gson();
		String sRequest = googleJson.toJson(request);
		this.queueChannel.basicPublish("", this.colasActivasWorkers.get(request.getIdWorker()), MessageProperties.PERSISTENT_TEXT_PLAIN,sRequest.getBytes("UTF-8") );
		log.info("Parte sobel enviada a worker "+ request.getIdWorker());
	}
	
	
	public BufferedImage joinImages(BufferedImage inImg) throws IOException {
		// cuando ya recibio todas las request con sus correspondientes imagenes Sobel.
		log.info("Uniendo las imagenes sobel parciales... ");
		int w = inImg.getWidth();
		int h = inImg.getHeight();
		BufferedImage imgSalida = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		Graphics unionImagenes = imgSalida.getGraphics();
		int sectorSize = 0;
		for (int i=0; i<this.workerRequests.size();i++) {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(this.workerRequests.get(i).getOutImg()));
			unionImagenes.drawImage(image, sectorSize, 0, null);
			sectorSize+=image.getWidth();
		}
		return imgSalida;
	}
	
	//cuenta la cantidad de imagenes recibidas
	public int imagesReceived() {
		int cont = 0;
		for (int i = 0 ; i < this.workerRequests.size(); i++ ){
			if (this.workerRequests.get(i).getState().equals(StateSobelRequest.DONE)) {cont++;}
		}
		log.info("Hay "+ cont + " imagenes recibidas de las "+this.workerRequests.size()+" enviadas");
		return cont;
	}
	
	//se van recibiendo las repsuestas
	@SuppressWarnings("static-access")
	public void waitingImagesSobel() throws IOException, ClassNotFoundException, InterruptedException {
		boolean salir = false;
		long currentTimeWait = 0;
		
		while (!salir) {
			if (imagesReceived()==this.workerRequests.size()) {// si llegaron todas las respuestas de los workers
				log.info("Han llegado todas las partes de la imagen resueltas en sobel.");
				salir = true;
				
			}else {
				if (maxWaitTime == currentTimeWait) {//si paso el maximo tiempo de espera para las respuestas
					log.info("Se sobrepaso el teimpo maximo de espera.");
					vefiryFallenWorkers();
					currentTimeWait = 0;
				}else {
					currentTimeWait+= this.periodWaitTime; // aumento para la proxima entrada al while
					Thread.sleep(this.periodWaitTime);
				}
			}
		}

	}
	
//si se supera el tiempo de espera y no tengo todas las respuestas	
  private void vefiryFallenWorkers() throws IOException {
	  log.info("Verificando Workers caidos...");
	for (int i = 0 ; i<this.workerRequests.size(); i++) {
		
		if (this.workerRequests.get(i).getState().equals(StateSobelRequest.PENDENT)){
			// elimino la cola correspondiente
			 log.info("Worker " + this.workerRequests.get(i).getIdWorker() + " no envio su respuesta a tiempo. Reasignando tarea...");
			log.info(this.colasActivasWorkers.get(this.workerRequests.get(i).getIdWorker()) + " eliminada");
			this.queueChannel.queueDelete(this.colasActivasWorkers.get(this.workerRequests.get(i).getIdWorker()));
			this.colasActivasWorkers.remove(this.workerRequests.get(i).getIdWorker()); 
			//reasigno la request a otro worker. Para eso agarro el primer Worker que encuentre que ya haya cumplido su tarea.
			boolean reasignado = false;
			do { // va a intentar asignarle un nuevo worker hasta que alguno quede ocioso.
				int nextW = nextIdleWorker(this.workerRequests.get(i).getIdWorker());	
				if (nextW!=-1) {
					reasignado = true;
					log.info(" Tarea reasignada a Worker "+ nextW );
					this.workerRequests.get(i).setIdWorker(nextW);
					sendImgToWorker(this.workerRequests.get(i));
				}
			}while (!reasignado);	
		}
	}
  }		
  
  public int nextIdleWorker(int antWID) {
	  boolean find = false;
	  int i = 0;
	  int wID = -1;
	  while ((!find)&&(i<this.workerRequests.size())) { 
		  if (this.workerRequests.get(i).getState().equals(StateSobelRequest.DONE) 
		     && (this.workerRequests.get(i).getIdWorker()!=antWID)){// Worker ocioso, ya entrego su parte.
			  		find = true;
			  		wID = this.workerRequests.get(i).getIdWorker();
		  }
		  i++;
	  }
	  return wID;
  }
  
//-----------------------------------------------------------------------------------------------------------------------//	
}
