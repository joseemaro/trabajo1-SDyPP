package ThingsRuneables;

import TP2SDPP.Extremo;

public class Extremo1 {
	public static void main(String[] args) {
		/*		Inicio Peer1 listen port 8001		*/
			new Thread(new Runnable() {
			    public void run() {
			    	Extremo.main(8001,"peers/nodo1/");
		    }
		}).start();
	}
}
