import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientProtocol 
{
	private int temp_key = 0;
	private int pre_key = 0;
	private int suc_key = 0;
	private String suc_IP = null;
	private String pre_IP = null;
	private boolean isFirstMessage = true;
	private String [] string_array = null;
	
	//Socket socket = new Socket();//initialise socket object to use throughout
	PrintWriter sender = null;//init sender
	BufferedReader receiver = null;//init receiver
	String fileKey = null;
	String command_part = null; //First section of any message - caps section of the message
	String ip_part = null;//Second section of any message received - The ip string part of the message
	String hash_part = null;//Third section of any message received - The hashed part of the message
	List<String> keyList = null;//list of ip's which store required file 
	
	
	//Function to get the initial supernode ID which also means we have started the sequence and must get random number
	public String getSuperNodeIP()
	{
		Random rand = new Random(); // Create the object. note seed is current time by default
		temp_key = rand.nextInt(Integer.MAX_VALUE) + 1; //+1 to make sure it isn't 0. this value is still way below our max
		return Neighbourhood.getSuperNodeIP();
	}
	
	//Function to decide what message to send within the join overlay protocol. 
	public String sendJoin()
	{
		if (isFirstMessage) // If we are talking to the supernode the first question is key
			return "KEY";
		else
			return "SUCCESSORSKEY";
	}
	
	//Function to decide which IP to send back and also what to do with the received information
	public String receiveJoin(String message)
	{
		if (isFirstMessage) // If we are talking to the supernode send back supernode IP to ask for successor
		{
			isFirstMessage = false; //no longer going to be talking to supernode
			suc_key = getKey(message);// set the initial suc_key to be the supernodes key (which will be changed to pre_key next loop)
			suc_IP = Neighbourhood.getSuperNodeIP();//set the initial successor IP as the supernode
			if (suc_key == temp_key) // if the random key was the supernodes key
				temp_key = temp_key + 1;
			return Neighbourhood.getSuperNodeIP();
		}
		else
		{
			pre_key = suc_key; //set previous key to what successor key was
			pre_IP = suc_IP;;// set the previous IP to what successors was
			suc_key = getKey(message);//set the new successor key
			suc_IP = getIP(message);//set theh new successor IP
			
			// if we are sitting in between the 2 we can slot in and exit or if we are the first to join
			if ((suc_key > temp_key && temp_key > pre_key) || (suc_IP.equals(Neighbourhood.getSuperNodeIP())) ) 
			{
				Neighbourhood.setMyId(Integer.toString(temp_key));	
				try 
				{
					Neighbourhood.setMyIp(InetAddress.getLocalHost().getHostAddress());//gets and saves the IP address
				} 
				catch (UnknownHostException e) {
					e.printStackTrace();
				}
				return "done";
			}
			else if (suc_key == temp_key) // if the key is taken, try the next sequential slot
				temp_key = temp_key + 1;
		}
		return getIP(message);
	}
	
	public boolean isSufficientForOverlay() 
	{
		String sucsucIP = Neighbourhood.getSucSucIp();
		String myIP = Neighbourhood.getMyIp();
		
		if(sucsucIP.equals(myIP))
			return false;
		else 
			return true;
	}
	
	//This function is in charge of saving the successor and predecessor and telling them to update their fields
	public void updateNeighbourhood(Socket socket)
	{
		//Update predecessor and successor
		Neighbourhood.setPreIp(pre_IP);
		Neighbourhood.setPreId(Integer.toString(pre_key));
		Neighbourhood.setSucIp(suc_IP);
		Neighbourhood.setSucId(Integer.toString(suc_key));
		String response = null;
		String[] IP = {pre_IP, suc_IP};
		String[] updateCommands = {"UPDATESUCCESSOR " + Neighbourhood.getMyId() + " " + Neighbourhood.getMyIp(), "UPDATEPREDECESSOR " + Neighbourhood.getMyId() + " " + Neighbourhood.getMyIp()};
				
		for(int i=0; i<2 ; i++)
		{
			try 
			{
				socket = new Socket(IP[i], 4020);
				sender = new PrintWriter(socket.getOutputStream(), true);
		        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
	
	        sender.println(updateCommands[i]);//send the right command
	        
	        try 
	        {
				response = receiver.readLine();//read the response
				if (!response.equals("ACK"));
				System.out.println("failed to tell Neighbourhood to update itself");
			} 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
		}
			
	}
	
	//Macro function for distributing a single file key
	public void distributeFileKeys(List<String> fileKeys, Socket socket)
	{
		ip_part = Neighbourhood.getSucSucId();//Statically access the sucSuccessor ip address		
		boolean isNodeFound = false;
		boolean isKeyStored = false;
		String response = null;
		
		//Store every key in the list (Greg)
		for (int i=0; i<fileKeys.size(); i++)
		{
			while(!isKeyStored)//while the key has not been stored in appropriate node...
			{	
				do //Initiate comms and receive comms until found the correct node to store key with
				{	
					try
					{
						socket = new Socket(ip_part,4017);
						sender = new PrintWriter(socket.getOutputStream(), true);
				        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
					}
					catch (UnknownHostException e) 
					{
						e.printStackTrace();
					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					 
					response = whoShouldHoldThisKey(fileKeys.get(i));
					
					isNodeFound = processResponseMessage(response);
				}
				while(!isNodeFound);//Exit loop when storage node is found
				
				isKeyStored = storeKey(fileKeys.get(i), socket);//Init comms, and send fileKey to the found node.
												//returns true if receive ACK 
				isNodeFound = false;//If not ACKed, must reset inner loop and continue with search for node.
			}
		}
		
		//try and close socket and read/write
		try 
		{
			receiver.close();
			sender.close();
			socket.close();
		} 
		catch (IOException e) { e.printStackTrace(); }	
	}
	
	public void retrieveFileKeyList(String fileKey, Socket socket)
	{
		ip_part = Neighbourhood.getSucSucId();//Statically access the sucSuccessor ip address		
		boolean isNodeFound = false;
		boolean isKeyListRetrieved = false;
		String response = null;
		
		while(!isKeyListRetrieved)//while the keylist has not been retrieved from appropriate node...
		{	
			do //Initiate comms and receive comms until found the correct node which stores keylist
			{	
				try
				{
					socket = new Socket(ip_part,4017);
					sender = new PrintWriter(socket.getOutputStream(), true);
			        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
				}
				catch (UnknownHostException e) 
				{
					e.printStackTrace();
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				response = whoShouldHoldThisKey(fileKey);
				
				isNodeFound = processResponseMessage(response);
			}
			while(!isNodeFound);//Exit loop when storage node is found
			
			isKeyListRetrieved = retrieveKeyList(fileKey);//Init comms, and retrieve keylist from node.
													   //returns true if receives NODESWITHLIST cmd. 
			isNodeFound = false;//NODESWITHLIST not received, reset inner loop and continue with search for node.
		}
	}
	
	public List<Integer> processIndexResponse(String message) 
	{
		List<Integer> indices = new ArrayList<Integer>();
		string_array = message.split(" "); //split the string by the " " = space parameter if only one word it returns that word
		for (int i = 0; i < string_array.length - 1; i++)
		{
			indices.add(i, Integer.parseInt(string_array[i+1]));//+1 to not worry about the command
		}
		return indices;
	}
	
	public String processPortResponse(String message) 
	{
		string_array = message.split(" "); //split the string by the " " = space parameter if only one word it returns that word
		return string_array[1];//return the port number to use in the http method
	}
	
	//CHECK ALIVE QUERY FUNCTIONS (greg)
	
	public String checkAliveQuery()
	{
		return "ALIVE";
	}

	public String checkAliveResponse()
	{	
		return "ACK";
	}
	
	public String getPredecessorQuery()
	{
		
		return "PREDECESSORSKEY";
	}
    
	public String getPredecessorResponse()
	{
		return "RETPREDECESSORSKEY";
	}
	
	public String getSuccessorQuery()
	{
		return "SUCCESSORSKEY";
	}
	
	public String getSuccessorResponse()
	{
		return "RETSUCCESSORSKEY";
	}
	
	
	public List<String> getKeyList()
	{
		return keyList;
	}

	//From Shappy
	
	public String getIndexQuery(String hash) 
	{
		return "REQUESTINDEXSOFHASH " + hash;
	}
	
	public String getPortNum(String hash, int index) 
	{
		return "REQUEST " + hash + " " + Integer.toString(index);
	}
	
	
	
	
	
	//HELPER PRIVATE FUNCTIONS (etai)
	
	private String whoShouldHoldThisKey(String fileKey)//Determine who to send the file key to
	{	
		String response = null;	
		sender.println("RESPONSIBLEKEY " + fileKey);
		try
		{
			response = receiver.readLine();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return response;
	}
	
	private void formatResponseMessage(String response)//Break up received string into components and assigns new values
	{
		String[] temp;//init an array of strings
		temp = response.split(" ");//Split message into substrings with "space" as a delimiter
		
		switch(temp.length)//Depending on message spaces, know the format of the message
		{
			case 1: 
				command_part = temp[0];
				break;
			case 2: 
				command_part = temp[0];
				ip_part = temp[1];
				break;
			case 3: 
				command_part = temp[0];
				if (command_part.equals("NODESWITHFILE"))//Checks scenario of only 1 IP being
				{										 //being returned - ambiguity resolution		
					keyList.add(temp[1]); 
					break;
				}
				hash_part = temp[1];
				ip_part = temp[2];
				break;
			default:
				command_part = temp[0];
				for(int i = 1; i < temp.length; i++)//Case of more than 3 strings can only be
				{									//the keylist response message
					keyList.add(temp[i]);//adding the ip's to the keylist
				}
				break;
		}	
		
	}
	
	private boolean processResponseMessage(String response)//logical flag manipulation for state of process
	{
		formatResponseMessage(response);//breaking up received message
		
		if (command_part.equals("THISNODERESPONSIBLE"))//found the node to store the fileKey with
		{
			return true; 
		}
		else if (command_part.equals("ACK"))//key storage is successful
		{
			return true;
		}
		else if (command_part.equals("NODESWITHLIST"))//keylist retrieved
		{
			return true;
		}
			
		else return false;
	}

	private boolean storeKey(String fileKey, Socket socket)//Sends key and returns true if storage is ACKed by server
	{
		try
		{
			socket = new Socket(ip_part,4017);
			sender = new PrintWriter(socket.getOutputStream(), true);
	        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
		}
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		boolean isAck = false;//boolean to return 
		String response = null;	
		sender.println("STOREKEY " + fileKey +" " + Neighbourhood.getMyId() + " " + Neighbourhood.getMyIp());
		try
		{
			response = receiver.readLine();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//TODO have not dealt with if this is not the right node in which case we receive a "ASK ####"
		isAck = processResponseMessage(response);//will be true if server ACKed storage of key
		sender.close();
		try {
			receiver.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isAck;
	}
	
	private boolean retrieveKeyList(String fileKey)//Retrieves keylist and returns true upon retrieval
	{
		boolean isRetrieved = false;//boolean to return 
		String response = null;	
		sender.println("NODELIST " + fileKey);
		try
		{
			response = receiver.readLine();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		isRetrieved = processResponseMessage(response);//will be true if server ACKed storage of key
		return isRetrieved;
	}
	
	
	//HELPER PRIVATE FUNCTIONS (shappy)
	
	private int getKey(String message)
	{
		string_array = message.split(" "); //split the string by the " " = space parameter if only one word it returns that word
		
		return Integer.parseInt(string_array[1]);// parse the key into an integer
	}
	
	private String getIP(String message)
	{
		string_array = message.split(" "); //split the string by the " ". we want second parameter which is IP
		return string_array[2];
	}

	

	

	


    
}