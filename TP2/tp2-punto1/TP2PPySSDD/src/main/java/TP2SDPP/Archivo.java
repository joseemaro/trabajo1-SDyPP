package TP2SDPP;

import java.io.Serializable;

public class Archivo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String name;
	byte[] content;
	
	public Archivo(String name) {
		this.setName(name);
	}
	public Archivo(String name, byte[] content) {
		this.setName(name);
		this.setContent(content);
	}

	private void setContent(byte[] content) {
		this.content = content;
	}
	public byte[] getContent() {
		return this.content;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}