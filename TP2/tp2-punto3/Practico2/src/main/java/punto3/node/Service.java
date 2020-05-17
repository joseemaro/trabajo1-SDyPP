package punto3.node;

public interface Service {	
	
	String getName();
	int getPort();
	Object execute(Object[] list);
}
