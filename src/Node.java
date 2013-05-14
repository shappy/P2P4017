import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Node 
{
	
	private MultiThreadedServer server = null;
	private Client client = null;

	
	public Node()
	{
		this.server = new MultiThreadedServer(Neighbourhood.getPort());
		this.client = new Client();
	}
	
	public static void main(String[] args) 
	{
	
		
		
		try
		{
			String ip = InetAddress.getLocalHost().getHostAddress();
			Neighbourhood.setMyIp(ip);
			System.out.println(Integer.parseInt("4") >= Integer.parseInt("4"));
			Thread.sleep(4444);
		} 
		catch (UnknownHostException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		Node node = new Node();
				
	}

}
