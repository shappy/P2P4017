
public class ServerProtocol {

	private DHT dht = new DHT();
	
	String ip_part = null;
	String command_part = null;
	String key_part = null;
	String clientId_part = null;
	int swarm_size = Neighbourhood.getSwarmSize();



    
    public String respond(String input) 
    {
    	String response = null;
    	formatQuery(input);
    	if(command_part.equals("KEY")){response = "RETKEY " + Neighbourhood.getMyId();}
    	
    	else if(command_part.equals("SUCCESSORSKEY")){response = "RETSUCCESSORSKEY " + Neighbourhood.getSucId() + " " + Neighbourhood.getSucIp();}
    	
    	else if(command_part.equals("PREDECESSORSKEY")){response = "RETPREDECESSORSKEY " + Neighbourhood.getPreId() + " " + Neighbourhood.getPreIp();}
    	
    	else if(command_part.equals("ALIVE")){response = "ACK";}
    	
    	else if(command_part.equals("GETSIZE")){response = "RETSIZE " + Neighbourhood.getSwarmSize();}
    	
    	else if(command_part.equals("UPDATESUCCESSOR")){response = "ACK"; Neighbourhood.setSucId(key_part);  Neighbourhood.setSucIp(ip_part); System.out.println("Neighbourhood's sucID is " + Neighbourhood.getSucId()); System.out.println("Neighbourhood's sucIP is " + Neighbourhood.getSucIp());}

    	else if(command_part.equals("UPDATEPREDECESSOR")){response = "ACK"; Neighbourhood.setPreId(key_part); Neighbourhood.setPreIp(ip_part);}

    	else if(command_part.equals("RESPONSIBLEKEY"))
    	{
    		if(Integer.parseInt(Neighbourhood.getSucId()) >= Integer.parseInt(key_part))
    		{response = "THISNODERESPONSIBLE " + Neighbourhood.getSucIp();}
    		
    		else if (Integer.parseInt(Neighbourhood.getSucId()) >= Integer.parseInt(key_part))
    		{response = "THISNODERESPONSIBLE " + Neighbourhood.getSucSucIp();}
    		
    		else {response = "ASK " + Neighbourhood.getSucSucIp();}
    	}
    	
    	else if(command_part.equals("STOREKEY"))
    	{
    		if (Integer.parseInt(Neighbourhood.getMyId()) >= Integer.parseInt(key_part) && !(Integer.parseInt(Neighbourhood.getPreId()) >= Integer.parseInt(key_part)))
    		{response = "ACK";}
    		else 
    		{response = "ASK " + Neighbourhood.getSucSucIp();}
    	}
    	
    	else if(command_part.equals("NODELIST"))
    	{
    		response = "NODESWITHFILE " + dht.getIpList(key_part).get(0);
    		
    		for(int i = 1; i < dht.getIpList(key_part).size(); i++ )
    		{
    			response = response + " " + dht.getIpList(key_part).get(i);
    		}
    	}
    		
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
				System.out.println("ip recieved is: " + ip_part);
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