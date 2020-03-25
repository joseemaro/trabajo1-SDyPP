package TP1.Punto4;

import java.io.Serializable;

/**
 * Representa el mensaje intercambiado. Tiene un origen, un destino y un cuerpo.
 * @author mariano
 *
 */
public class Mensaje implements Serializable{

	private String origen;
	
	private String destino;
	
	private String cuerpo;

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getDestino() {
		return destino;
	}

	public void setDestino(String destino) {
		this.destino = destino;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}
	
	
	@Override
	public String toString() {
		String msg = "Origen: " + this.origen + "\n";
		msg+=("Destino: " + this.destino + "\n");
		msg+=("Mensaje: " + this.cuerpo+ "\n");
		return msg;
	}
}

