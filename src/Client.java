import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable
{
	Thread t;
	private checkAlive maintainNeighbourhood; 
		
	public Client()
	{
		t = new Thread(this);
		maintainNeighbourhood = new checkAlive();
		t.start();
	}	
	
	public void run() 
	{ 
		System.out.println("Inside client");
		
	}
}


