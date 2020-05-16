package ThingsRuneables;

import TP2SDPP.Master1;

public class Maestro1 {
	public static void main(String[] args) {
		/*		Inicio Master listen port 9000		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Master1.main(9000);
		    }
		}).start();
	}

}
