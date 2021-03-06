import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

public class ClientProtocol 
{
	private Neighbourhood neighbourhood = new Neighbourhood();
	private int temp_key = 0;
	private int pre_key = 0;
	private int suc_key = 0;
	private boolean isFirstMessage = true;
	private String [] string_array = null;
	
	Socket socket = new Socket();//initialise socket object to use throughout
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
		return neighbourhood.getSuperNodeIP();
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
			if (suc_key == temp_key) // if the random key was the supernodes key
				temp_key = temp_key + 1;
			return neighbourhood.getSuperNodeIP();
		}
		else
		{
			pre_key = suc_key; //set previous key to what successor key was
			suc_key = getKey(message);//set the new successor key
			
			if (suc_key > temp_key && temp_key > pre_key) // if we are sitting in between the 2 we can slot in and exit
			{
				Neighbourhood.setMyId(temp_key);
				return null;
			}
			else if (suc_key == temp_key) // if the key is taken, try the next sequential slot
				temp_key = temp_key + 1;
		}
		return getIP(message);
		

	}
	

	//Macro function for distributing a single file key
	public void distributeFileKey(String fileKey)
	{
		ip_part = Neighbourhood.getSucSucId();//Statically access the sucSuccessor ip address		
		boolean isNodeFound = false;
		boolean isKeyStored = false;
		String response = null;
		
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
				
				response = whoShouldHoldThisKey(fileKey);
				
				isNodeFound = processResponseMessage(response);
			}
			while(!isNodeFound);//Exit loop when storage node is found
			
			isKeyStored = storeKey(fileKey);//Init comms, and send fileKey to the found node.
											//returns true if receive ACK 
			isNodeFound = false;//If not ACKed, must reset inner loop and continue with search for node.
		}
	}
	
	public void RetreiveFileKeyList(String fileKey)
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
			case 1: command_part = temp[0];
					break;
			case 2: command_part = temp[0];
					ip_part = temp[1];
					break;
			case 3: command_part = temp[0];
					if (command_part.equals("NODESWITHFILE"))//Checks scenario of only 1 IP being
					{										 //being returned - ambiguity resolution		
						keyList.add(temp[1]); 
						break;
					}
					hash_part = temp[1];
					ip_part = temp[2];
					break;
			default:for(int i = 1; i < temp.length; i++)//Case of more than 3 strings can only be
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

	private boolean storeKey(String fileKey)//Sends key and returns true if storage is ACKed by server
	{
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
		
		isAck = processResponseMessage(response);//will be true if server ACKed storage of key
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
		
		return Integer.parseInt(string_array[0]);// parse the key into an integer
	}
	
	private String getIP(String message)
	{
		string_array = message.split(" "); //split the string by the " ". we want second parameter which is IP
		return string_array[1];
	}
	

	


    
}