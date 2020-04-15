package TP1.Punto3;

import java.io.Serializable;

/**
 * Funcion que ofrece el servidor. Identifica una funcion o <i>método</i>
 * particular, como enviar o recibir del server, y un cuerpo. 
 * @author mariano
 *
 */
public class Funcion implements Serializable{

	private int id;
	
	private String cuerpo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}
	
}

