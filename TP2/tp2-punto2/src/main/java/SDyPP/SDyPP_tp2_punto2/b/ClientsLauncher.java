package SDyPP.SDyPP_tp2_punto2.b;

import java.util.Random;

public class ClientsLauncher {

	public static void main(String[] args) {
		
		Random r = new Random();				
		int randomNum = r.nextInt(5) + 5; // Entre 5 y 10
		
		if (args.length > 0) {
			try {
				randomNum = Integer.parseInt(args[0]);
			}
			catch (Exception e) {				
				System.out.println("Argumento invAlido. Levantando " + randomNum + " clientes.");
			}
		}					
		
		for (int i = 0; i < randomNum; i++) {
			ClienteDeposito cliDepo = new ClienteDeposito(9000);			
			cliDepo.depositar();			
			
			ClienteExtraccion cliExtra = new ClienteExtraccion(9090);
			cliExtra.extraer();
			
		}
	}

}
