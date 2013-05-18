import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

//Greg
public class CheckAlive implements Runnable 
{
	//Data Members
	Thread t;
	private final int timeoutDuration = 15000; //milliseconds
	private final int checkAlivePause = 3000; //milliseconds
	private ClientProtocol protocol;
	List<String> keyIndex = new ArrayList<String>();
	private Socket socket = null;
	private PrintWriter sender = null;
	private BufferedReader receiver = null;

	//Functions
	public CheckAlive()
	{
		t = new Thread(this); //Assign this object to its own thread
		protocol = new ClientProtocol();
		t.start();
		System.out.println("In CheckAlive Thread");
	}

	public void run()
	{
		boolean flag = true;
		while(flag)
		{
			System.out.println("In CheckAlive Run");
			//TODO: maybe introduce a delay
			//Check that all neighbours are alive
			if(!isAlive(Neighbourhood.getPreIp())) //Predecessor
				updatePredecessor();
			
			if (Neighbourhood.getPrePreId() != Neighbourhood.getMyId()) //so we're not talking to ourselves
			{
				if(!isAlive(Neighbourhood.getPrePreIp())) //PrePredecessor
					updatePrePredecessor();
			}
			
			if(!isAlive(Neighbourhood.getSucIp())) //Successor
				updateSuccessor();
			
			if (Neighbourhood.getSucSucId() != Neighbourhood.getMyId()) //so we're not talking to ourselves
				
			{
				if(!isAlive(Neighbourhood.getSucSucIp())) //SucSuccessor
					updateSucSuccessor();
			}
			
			//Check that all nodes tracking this node are alive
			 keyIndex = Neighbourhood.getIpList();
			 for (int i=0; i<keyIndex.size(); i++)
			 {
				 List<String> ipList = Neighbourhood.getKeyList(keyIndex.get(i));
				 
				 for(int j = 0; j < ipList.size(); j++)
				 {
					 if(ipList.get(j) != Neighbourhood.getMyIp())
					 {
						 if(!isAlive(ipList.get(j)))
						 {
							 protocol.distributeFileKeys(socket);
						 }
					 }
				 }				 
			 }
			
		}
	}


	private boolean isAlive(String IPadr)

	{
		boolean result = true;
		String responseFromNode = null;

		//Initialise socket and read/write 
		try 
		{
			socket = new Socket();
			socket.connect(new InetSocketAddress(IPadr,Neighbourhood.getPort()),timeoutDuration); //set the timeout duration
			sender = new PrintWriter(socket.getOutputStream(),true);
			receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }

		//Ask the node if it is alive 
		sender.println(protocol.checkAliveQuery());

		//Listen for a reply
		try 
		{
			responseFromNode = receiver.readLine();
		} 
		catch (IOException e1) { e1.printStackTrace(); }

		//Evaluate node reply 
		//TODO: timeout might return something funky
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
			socket = new Socket(Neighbourhood.getPrePreIp(),Neighbourhood.getPort()); //speaking to pre-predecessor
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
			Neighbourhood.setPreId(Neighbourhood.getPrePreId());
			Neighbourhood.setPreIp(Neighbourhood.getPrePreIp());

			//Assign pre-pre-predecessor to pre-predecessor
			String[] usefulResponse = response.split(" ");
			Neighbourhood.setPrePreId(usefulResponse[1]);
			Neighbourhood.setPrePreIp(usefulResponse[2]);		
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
		} 
		catch (InterruptedException e1) { e1.printStackTrace(); }

		//Initialise socket and read/write
		try 
		{
			socket = new Socket(Neighbourhood.getPreIp(),Neighbourhood.getPort()); //speaking to predecessor
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
			Neighbourhood.setPrePreId(usefulResponse[1]);
			Neighbourhood.setPrePreIp(usefulResponse[2]);		
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
			socket = new Socket(Neighbourhood.getSucSucIp(),Neighbourhood.getPort()); //speaking to suc-successor
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
			Neighbourhood.setSucId(Neighbourhood.getSucSucId());
			Neighbourhood.setSucIp(Neighbourhood.getSucSucIp());

			//Assign suc-successor's sucessor to my suc-successor
			String[] usefulResponse = response.split(" ");
			Neighbourhood.setSucSucId(usefulResponse[1]);
			Neighbourhood.setSucSucIp(usefulResponse[2]);
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
			socket = new Socket(Neighbourhood.getSucIp(),Neighbourhood.getPort()); //speaking to suc-successor
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
			Neighbourhood.setSucSucId(usefulResponse[1]);
			Neighbourhood.setSucSucIp(usefulResponse[2]);
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