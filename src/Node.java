
public class Node 
{
	//private MultiThreadedServer server = null;
	private Client client = null;

	
	public Node()
	{
		//this.server = new MultiThreadedServer(4017);
		this.client = new Client();
	}
	
	public static void main(String[] args) 
	{
		
		//Will have to initialise the values of the Neighbourhod members here
		Node node = new Node();
		
				
	}

}
