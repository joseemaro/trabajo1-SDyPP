package TP1.Punto4;

import java.util.ArrayList;
import java.util.List;

public class ListaMensajes {
	
	private List<Mensaje> mensajes;
	
	public ListaMensajes() {
		this.mensajes = new ArrayList<Mensaje>();
	}

	public List<Mensaje> getMensajesPara(String destino) {
		List<Mensaje> rta = new ArrayList<Mensaje>();		
		this.mensajes.forEach(m -> {
			if (m.getDestino().equals(destino)) {				
				rta.add(m);
			}
		});
		
		return rta;
	}

	public void add(Mensaje m) {
		this.mensajes.add(m);
		
	}

	public void delete(String destino) {
		mensajes.removeIf(m -> m.getDestino().equals(destino));		
	}
	
	
}
