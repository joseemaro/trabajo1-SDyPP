package TP1.Punto4;

import java.io.Serializable;

/**
 * Funcion que ofrece el servidor. Identifica una funcion o <i>método</i>
 * particular, como enviar o recibir del server, y un cuerpo. 
 * @author mariano
 *
 */



public class Funcion implements Serializable{


	private TiposFuncion id;
	
	private String cuerpo;

	
	public TiposFuncion getId() {
		return id;
	}

	public void setId(TiposFuncion id) {
		this.id = id;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}
	
}

