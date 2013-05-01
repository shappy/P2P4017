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
	private final long checkAlivePause = 3000; //3000 mS = 3 seconds
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
			if(!isAlive(neighbours.getPrePreIp())) //SucSuccessor
				updateSucSuccessor();
			
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
			String[] usefulResponse = response.split(" ");
			neighbours.setPrePreId(usefulResponse[1]);
			neighbours.setPrePreIp(usefulResponse[2]);		
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
		//Pause - wait some time for my predecessor to detect that his precdecessor
		//(my pre-predeccessor) has fallen off the network and to update his neighbourhood.
		try 
		{
			Thread.sleep(checkAlivePause);
		} catch (InterruptedException e1) { e1.printStackTrace(); }
		
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
			String[] usefulResponse = response.split(" ");
			neighbours.setPrePreId(usefulResponse[1]);
			neighbours.setPrePreIp(usefulResponse[2]);		
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
			
			//Assign suc-successor's sucessor to my suc-successor
			String[] usefulResponse = response.split(" ");
			neighbours.setSucSucId(usefulResponse[1]);
			neighbours.setSucSucIp(usefulResponse[2]);
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

	private void updateSucSuccessor()
	{
		//Pause - wait for my successor to realise that his successor
		//(my sucsuccessor) has fallen off the overlay and for him to update his neighbourhood
		try 
		{
			Thread.sleep(checkAlivePause);
		} catch (InterruptedException e1) { e1.printStackTrace(); }
		
		//Initialise socket and read/write
		try 
		{
			socket = new Socket(neighbours.getSucIp(),4017); //speaking to suc-successor
			sender = new PrintWriter(socket.getOutputStream(),true);
			receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Ask for his successor
		sender.println(protocol.getSuccessorQuery());
		
		//Listen for his response
		String response = null;
		try 
		{
			response = receiver.readLine();
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		//If correct reply, update
		if(response.contains(protocol.getSuccessorResponse()))
		{
			//Set my suc-successor as my successors successor 
			String[] usefulResponse = response.split(" ");
			neighbours.setSucSucId(usefulResponse[1]);
			neighbours.setSucSucIp(usefulResponse[2]);
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
