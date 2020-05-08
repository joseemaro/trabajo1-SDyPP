package Punto4;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SobelRequest implements Serializable{

		private int idWorker;//id

		private byte[] inParcialImg;	//imagen parcial, string de bytes	
		private byte[] outImg;			//imagen de salida
		
		public SobelRequest (int idWorker, byte[] inParcialImg) {
			this.idWorker = idWorker;
			this.inParcialImg = inParcialImg;
		}

		public int getIdWorker() {return this.idWorker;}
		public byte[] getInParcialImg() {return this.inParcialImg;}
		public byte[] getOutImg() {return this.outImg;}
		
		public void setOutImg(byte[] outImg) {this.outImg = outImg;}
		public void setIdWorker(int idWorker) {this.idWorker = idWorker;}
}
