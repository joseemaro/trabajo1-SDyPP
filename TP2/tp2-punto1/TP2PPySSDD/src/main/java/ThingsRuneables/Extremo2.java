package ThingsRuneables;

import TP2SDPP.Extremo;

public class Extremo2 {
	public static void main(String[] args) {
		/*		Inicio Peer2 listen port 8002		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Extremo.main(8002,"peers/nodo2/");
		    }
		}).start();
	}
}
