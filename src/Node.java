import java.util.Random;


public class Node 
{
	
	private MultiThreadedServer server = null;
	//private Client client = null;

	
	public Node()
	{
		this.server = new MultiThreadedServer(4018);
		//this.client = new Client();
	}
	
	public static void main(String[] args) 
	{
		Node node = new Node();
				
	}

}
