package SDyPP.SDyPP_tp2_punto2.a;

import java.util.Random;

public class Main {

	public static void main(String[] args) {
		// Levanta random clientes y busca provocar la falla		
		Random r = new Random();
		
		int randomNum = r.nextInt(10) + 5; // Entre 5 y 15
		
		for (int i = 0; i < randomNum; i++) {
			ClienteDeposito cliDepo = new ClienteDeposito(9000);			
			cliDepo.depositar();			
			
			ClienteExtraccion cliExtra = new ClienteExtraccion(9090);
			cliExtra.extraer();
			
		}
	}

}
