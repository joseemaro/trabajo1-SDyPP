package Punto4;

import java.awt.Graphics;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

	@SuppressWarnings("unused")
	private String ip;
	private int port;
	private final static Logger log = LoggerFactory.getLogger(ServerSobel.class);
	private Map<Integer, String> colasWorkersActivas; // par PuertoWorker-> NombreCola 
	private ArrayList<WorkerSobel> workers;
	private Map<Integer,BufferedImage> imgFinalizadas; // par IdWorker -> ImagenSobel
	@SuppressWarnings("unused")
	private ConnectionFactory connectionFactory;
	@SuppressWarnings("unused")
	private Connection queueConnection;
	private Channel queueChannel;
    private String nombreColaSalida;
	private static final int sizeBorderSobel = 1;
	
	public ServerSobel(String ip, int port, ArrayList<WorkerSobel> workers, Channel queueChannel) throws NotBoundException, IOException, InterruptedException {
		this.port = port;
		this.colasWorkersActivas = new HashMap<Integer,String>();
		this.workers= workers;
		this.imgFinalizadas = new HashMap<Integer,BufferedImage>();
		this.nombreColaSalida = "QUEUE_SOBEL_OUT";
		this.queueChannel = queueChannel;
		
		//INICIA EL SOBEL RMI PARA EL CLIENTE
		this.startSobelRMIforClient();
		//CONFIGURA CONEXION A RABBIT
		configureConnectionToRabbit();
		//INICIA COLA DE WORKERS
		this.startWorkersQueues();
	}
	
	public static void main(String[] args) throws IOException, NotBoundException, InterruptedException, TimeoutException {
		Logger log = LoggerFactory.getLogger(ServerSobel.class);
		log.info("-X-X-X-X-X-X-  SE HA INICIADO EL SERVIDOR  -X-X-X--X-X-X-");
		System.out.println(" ");
		System.out.println("INGRESE LA CANTIDAD DE SERVIDORES QUE DESEA USAR");
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		//INDICA LA CANTIDAD DE SERVIDORES QUE SE CREAN
		int qWorkers=  scanner.nextInt();
		int portInicialWorker = 90;
		ArrayList<WorkerSobel> listaWorkers = new ArrayList<WorkerSobel>();
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("localhost");
		Connection queueConnection = connectionFactory.newConnection();
		Channel queueChannel = queueConnection.createChannel();
		
		//CREO LOS WORKERS
		for (int i =0 ; i<qWorkers; i++) {	
			@SuppressWarnings("unused")
			int portW = portInicialWorker + i;
			WorkerSobel ws = new WorkerSobel(i, queueChannel);
			listaWorkers.add(ws);
		}
		
		System.out.println("");
		int portRMIserver = 1200;
		//CREO EL SERBER SOBEL
		@SuppressWarnings("unused")
		ServerSobel ss = new ServerSobel("localhost",portRMIserver,listaWorkers, queueChannel);
					
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
		SobelRequest response = googleJson.fromJson(new String(delivery.getBody(),"UTF-8"), SobelRequest.class);
		log.info("ServerSobel recibio una imagen del worker "+ response.getIdWorker());
        
		//ACA RECIBE LA IMAGEN PARCIAL YA TRANSFORMADA CON LOS BORDES
	    BufferedImage imgSalida = ImageIO.read(new ByteArrayInputStream(response.getOutImg()));
    	int w = imgSalida.getWidth();
		@SuppressWarnings("unused")
		int h = imgSalida.getHeight();
		//TOMA EL ANCHO Y LARGO Y CREA UNA IMAGEN
		@SuppressWarnings("static-access")
		BufferedImage subImg = imgSalida.getSubimage(this.sizeBorderSobel, imgSalida.getMinY(),w-(this.sizeBorderSobel*2) , imgSalida.getHeight());
	    //GUARDO LA IAMGEN PROCESADA
        this.imgFinalizadas.put(response.getIdWorker(),subImg);
    };
    
    
	private void startSobelRMIforClient() throws RemoteException {
		//CREA SERVER EN UN PUERTO
		Registry server = LocateRegistry.createRegistry(this.port);
		log.info("-----ServerSobel iniciado en el puerto "+this.port+" -----");
		//CREA SERVICIO EN UN PUERTO
		RemoteInt serviceSobel = (RemoteInt) UnicastRemoteObject.exportObject(this, 8000);
		log.info("-----ServerSobel asociado a puerto "+8000+" -----");
		//BIND ENTRE SERVICO Y sobelImagenes
		server.rebind("sobelImagenes", serviceSobel);
		log.info("-----Server bind realizado -----");
		
		log.info("-----se ha establecido la conexion con el cliente rmi");
		System.out.println("");

	}

	
	public void startWorkersQueues() throws NotBoundException, IOException, InterruptedException {
		//de cada worker toma su id y su cola y lo agrega a la cola.
		for (int i =0 ; i<this.workers.size(); i++) {	
			colasWorkersActivas.put(this.workers.get(i).getIdWorker(),this.workers.get(i).getQueueName() );
		}

		log.info("-- ServerSobel creo todas las colas en sus workers");
	}
	
	
	@SuppressWarnings("static-access")
	public byte[] sobelDistribuido(byte[] image) throws IOException, InterruptedException, NotBoundException, ClassNotFoundException {
		log.info("--INGRESO A PROCESO 'SOBELDISTRIBUIDO' ENTRE CLIENTE Y SERVERSOBEL  --");
		//leo imagen del cliente
		BufferedImage imgEntrante = ImageIO.read(new ByteArrayInputStream(image));
		BufferedImage imgSalida; //imagen final que paso a SobelClient
		BufferedImage imgParcialWorker; //Parte de la imagen que paso al Worker
		@SuppressWarnings("unused")
		BufferedImage imgParcialRecibida;// Parte de la imagen que recibo del Worker	
		
		int qSector = (int)(imgEntrante.getWidth()/this.workers.size());
		int currentSector = 0;
		int currentWorker = 0;
	
		//variables de imagen resultado
		int w = imgEntrante.getWidth(); 
		int h = imgEntrante.getHeight();
		imgSalida = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int qPart= 1;
		// le voy a asignar una parte de la imagen a cada worker
		while (qPart <= this.workers.size() ) {
			int proxSectorSize = currentSector+(2*qSector);
			int borderIzq=0;
			if (currentSector!=0) {
				borderIzq = this.sizeBorderSobel;
			}
			//voy a dividir en partes, por lo que si alguna parte no se divide exactamente, se asigna al ultimo worker
			if (proxSectorSize>imgEntrante.getWidth()) {// cuando la division en partes entre los workers no da exacta, se asignara el resto al ultimo Worker
				int proxSector = currentSector+qSector;
			    int hastaFinImg = qSector+(imgEntrante.getWidth() - proxSector);
			    imgParcialWorker = imgEntrante.getSubimage(currentSector-borderIzq, imgEntrante.getMinY(), hastaFinImg+(this.sizeBorderSobel), imgEntrante.getHeight()); //funcion para partir la imagen.			
			}else {// caso normal
				imgParcialWorker = imgEntrante.getSubimage(currentSector-borderIzq, imgEntrante.getMinY(), qSector+(this.sizeBorderSobel*2), imgEntrante.getHeight()); 
			}
			
			ByteArrayOutputStream inImgParam = new ByteArrayOutputStream();
			ImageIO.write(imgParcialWorker, "jpg", inImgParam);
			//se le envia a cada worker el string de bytes para que los procese
			SobelRequest request = new SobelRequest(this.workers.get(currentWorker).getIdWorker(), inImgParam.toByteArray());
			//envia a Worker 
			Gson googleJson = new Gson();
			String sRequest = googleJson.toJson(request);
			this.queueChannel.basicPublish("", this.colasWorkersActivas.get(currentWorker), MessageProperties.PERSISTENT_TEXT_PLAIN,sRequest.getBytes("UTF-8") );
			log.info("Parte sobel"+qPart+" enviada a worker "+ request.getIdWorker());
			
			qPart++;
			currentSector+= qSector;
			currentWorker++;
			if (currentWorker==this.workers.size()) {
				currentWorker = 0;
			}
		}
		//espera las imagenes
		waitingImagesSobel();
		//une las imagenes
		imgSalida = joinImages(imgEntrante);
		
		ByteArrayOutputStream imgResult = new ByteArrayOutputStream();
		ImageIO.write(imgSalida, "jpg", imgResult);
		log.info("SE HA ENVIADO EL RESULTADO DEL SOBEL AL CLIENTE MEDIANTE EL USO DE RMI");
		return imgResult.toByteArray();
	}
	
	//une las imagenes parciales y devuelve la completa
	public BufferedImage joinImages(BufferedImage inImg) {	
		log.info("Uniendo las imagenes sobel parciales... ");
		int w = inImg.getWidth();
		int h = inImg.getHeight();
		BufferedImage imgSalida = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		Graphics unirImagenes = imgSalida.getGraphics();
		int sectorSize = 0;
		for (int i=0; i<this.colasWorkersActivas.size();i++) {
			unirImagenes.drawImage(this.imgFinalizadas.get(this.workers.get(i).getIdWorker()), sectorSize, 0, null);
			sectorSize+=this.imgFinalizadas.get(this.workers.get(i).getIdWorker()).getWidth();
		}
		return imgSalida;
	}
	
	//espera recibir todas las soluciones de las imagenes
	public void waitingImagesSobel() throws IOException, ClassNotFoundException, InterruptedException {
		boolean salir = false;
		while (!salir) {
			System.out.println("largo de finishedImgs: " + this.imgFinalizadas.size());
			if (this.imgFinalizadas.size()==this.colasWorkersActivas.size()) {
				log.info("TODAS LAS PARTES DEL SOBEL HAN SIDO RECIBIDAS");
				salir = true;
			}else {
				Thread.sleep(100);
			}
		}

	}

	
	public String proceso() throws IOException, InterruptedException {
		return "SE ESTA LLEVANDO A CABO EL PROCESO EN EL SERVER SOBEL";
	}

	
}
