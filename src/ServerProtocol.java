
public class ServerProtocol {

	String ip_part = null;
	String command_part = null;
	String key_part = null;
	String clientId_part = null;
	int swarm_size = Neighbourhood.getSwarmSize();

	private Neighbourhood neighbourhood;

	public ServerProtocol()
	{
		neighbourhood = new Neighbourhood();
	}
    
    public String respond(String input) 
    {
    	String response = null;
    	formatQuery(input);
    	if(command_part.equals("KEY")){response = "RETKEY " + Neighbourhood.getMyId();}
    	
    	else if(command_part.equals("SUCCESSORKEY")){response = "RETSUCCESSORKEY " + Neighbourhood.getSucId() + " " + Neighbourhood.getSucIp();}
    	
    	else if(command_part.equals("PREDECESSOR")){response = "RETPREDECESSOR " + Neighbourhood.getPreId() + " " + Neighbourhood.getPreIp();}
    	
    	else if(command_part.equals("ALIVE")){response = "ACK";}
    	
    	else if(command_part.equals("GETSIZE")){response = "RETSIZE " + Neighbourhood.getSwarmSize();}
    	
    	//TODO:other reponses to queries for key responsibility
    	
    	
    	return response;
    }
    
    private void formatQuery(String input)
    {
    	String[] temp;//init an array of strings
		temp = input.split(" ");//Split message into substrings with "space" as a delimiter

		switch(temp.length)//Depending on message spaces, know the format of the message
		{
			case 1: 
				command_part = temp[0];
				break;
			case 2: 
				command_part = temp[0];
				if(command_part.equals("SIZE"))
				{
					swarm_size = Integer.parseInt(temp[1]);
					break;
				}
				else key_part = temp[1];
				break;
			case 3: 
				command_part = temp[0];
				key_part = temp[1];
				ip_part = temp[2];
				break;
			case 4:
				command_part = temp[0];
				key_part = temp[1];
				clientId_part = temp[2];
				ip_part = temp[3];
				break;
			default:
				break;
		}		
    }
    
}