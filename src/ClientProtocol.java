import java.util.Random;

public class ClientProtocol 
{
	private Neighbourhood neighbourhood = new Neighbourhood();
	private int temp_key = 0;
	private int pre_key = 0;
	private int suc_key = 0;
	private boolean isFirstMessage = true;
	private String [] string_array = null;
	
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