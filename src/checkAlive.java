import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.String;


public class checkAlive implements Runnable 
{
	//Data Members
	Thread t;
	private Neighbourhood neighbours;
	private final long timeoutDuration = 15000; //15 000 mS = 15 seconds  
	private ClientProtocol protocol;
	private Socket socket = null;
	private PrintWriter sender = null;
	private BufferedReader receiver = null;
	
	
	//Functions
	public checkAlive()
	{
		t = new Thread(this); //Assign this object to its own thread
		neighbours = new Neighbourhood();
		protocol = new ClientProtocol();
		System.out.println("Inside checkAlive");
		t.start();
	}
	
	public void run()
	{
		boolean flag = true;
		while(flag)
		{
			if(!isAlive(neighbours.getPreIp())) //Predecessor
				updatePredecessor();
			if(!isAlive(neighbours.getPrePreIp())) //PrePredecessor
				updatePrePredecessor();
			if(!isAlive(neighbours.getSucIp())) //Successor
				updateSuccessor();
			
		}
	}
	
		
	private boolean isAlive(String IPadr)

	{
		String responseFromNode = null;
		boolean result = true;
		
		//Initialise socket and read/write 
		try 
		{
			socket = new Socket(IPadr,4017);
			sender = new PrintWriter(socket.getOutputStream(),true);
			receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
			
		//Ask the node if it is alive 
		sender.println(protocol.checkAliveQuery());
		
		//Listen for a reply for timeoutDuration seconds
		long startTime = System.currentTimeMillis();
		long timeout = startTime + timeoutDuration;
		while(System.currentTimeMillis() < timeout)
		{
			try //will it wait here?
			{
				responseFromNode = receiver.readLine();
			} 
			catch (IOException e) { e.printStackTrace(); }
			
			//Break if the node answered
			if (responseFromNode != null)
				break;		
		}
		
		//Evaluate node reply
		if(responseFromNode == null)
			result = false;
		else if(responseFromNode == protocol.checkAliveResponse())
			result = true;
		
		//Close socket and read/write
		try 
		{
			receiver.close();
			sender.close();
			socket.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		return result;		
	}

	private void updatePredecessor()
	{
		//Initialise socket and read/write
		try 
		{
			socket = new Socket(neighbours.getPrePreIp(),4017); //speaking to pre-predecessor
			sender = new PrintWriter(socket.getOutputStream(),true);
			receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Ask for his predecessor
		sender.println(protocol.getPredecessorQuery());
		
		//Listen for his predecessor - assuming that he is alive
		String response = null;
		try 
		{
			response = receiver.readLine();
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		//If correct reply, update predecessor
		if(response.contains(protocol.getPredecessorResponse()))
		{
			//Assign pre-predecessor to predecessor
			neighbours.setPreId(neighbours.getPrePreId());
			neighbours.setPreIp(neighbours.getPrePreIp());
			
			//Assign pre-pre-predecessor to pre-predecessor
			int whiteSpace1 = response.indexOf(" ");
			String usefulResponse = response.substring(whiteSpace1+1,response.length());
			int whiteSpace2 = usefulResponse.indexOf(" ");
			neighbours.setPrePreId(usefulResponse.substring(0,whiteSpace2));
			neighbours.setPrePreIp(usefulResponse.substring(whiteSpace2+1,usefulResponse.length()));		
		}
		
		//Close socket and read/write
		try 
		{
			receiver.close();
			sender.close();
			socket.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
			
	}
	
	private void updatePrePredecessor()
	{
		
		//Initialise socket and read/write
		try 
		{
			socket = new Socket(neighbours.getPreIp(),4017); //speaking to predecessor
			sender = new PrintWriter(socket.getOutputStream(),true);
			receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Ask for his predecessor
		sender.println(protocol.getPredecessorQuery());
		
		//Listen for his predecessor
		String response = null;
		try 
		{
			response = receiver.readLine();
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		//If correct reply, update
		if(response.contains(protocol.getPredecessorResponse()))
		{
			int whiteSpace1 = response.indexOf(" ");
			String usefulResponse = response.substring(whiteSpace1+1,response.length());
			int whiteSpace2 = usefulResponse.indexOf(" ");
			neighbours.setPrePreId(usefulResponse.substring(0,whiteSpace2));
			neighbours.setPrePreIp(usefulResponse.substring(whiteSpace2+1,usefulResponse.length()));		
		}
		
		//Close socket and read/write
		try 
		{
			receiver.close();
			sender.close();
			socket.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
			
	}

	private void updateSuccessor()
	{
		//Initialise socket and read/write
		try 
		{
			socket = new Socket(neighbours.getSucSucIp(),4017); //speaking to suc-successor
			sender = new PrintWriter(socket.getOutputStream(),true);
			receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Ask for his successor
		sender.println(protocol.getSuccessorQuery());
		
		//Listen for his successor
		String response = null;
		try 
		{
			response = receiver.readLine();
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		//If correct reply, update
		if(response.contains(protocol.getSuccessorResponse()))
		{
			//Assign suc-successor to successor
			neighbours.setSucId(neighbours.getSucSucId());
			neighbours.setSucIp(neighbours.getSucSucIp());
			
			//Assign sucsuccessor's sucessor to the sucsuccessor
			int firstWhiteSpace = response.indexOf(" ");
			String usefulResponse = response.substring(firstWhiteSpace+1,response.length());
			int secondWhiteSpace = usefulResponse.indexOf(" ");
			neighbours.setSucSucId(usefulResponse.substring(0,secondWhiteSpace));
			neighbours.setSucSucIp(usefulResponse.substring(secondWhiteSpace+1,usefulResponse.length()));
		}
		
		//Close socket and read/write
		try 
		{
			receiver.close();
			sender.close();
			socket.close();
		} 
		catch (IOException e) { e.printStackTrace(); }

	}



}
