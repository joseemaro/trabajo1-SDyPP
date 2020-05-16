package ThingsRuneables;

import TP2SDPP.Master1;

public class Maestro2 {
	public static void main(String[] args) {
		/*		Inicio Master listen port 9001		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Master1.main(9001);
		    }
		}).start();
	}
}
