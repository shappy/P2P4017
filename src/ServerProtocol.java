import java.io.IOException;

import java.io.*;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//Etai
public class ServerProtocol {
	
	String ip_part = "";
	String command_part = "";
	String key_part = "";
    String hash_part = ""; //greg
    String index_part = ""; //greg
	String clientId_part = "";
	String indexURL = "";
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
    		
    		if (Integer.parseInt(Neighbourhood.getMyId()) >= Integer.parseInt(key_part) && (Integer.parseInt(Neighbourhood.getPreId()) < Integer.parseInt(key_part)))
    		{response = "THISNODERESPONSIBLE "  + Neighbourhood.getMyIp();}

    		//if my sucessor's id is < than mine, I am the largest id on the overlay and therefore responsible
    		else if(Integer.parseInt(Neighbourhood.getSucId()) < Integer.parseInt(Neighbourhood.getMyId()))
    		{response = "THISNODERESPONSIBLE "  + Neighbourhood.getMyIp();}
    		
    		else if(Integer.parseInt(Neighbourhood.getSucId()) >= Integer.parseInt(key_part) && Integer.parseInt(Neighbourhood.getMyId()) < Integer.parseInt(key_part))
    		{response = "THISNODERESPONSIBLE " + Neighbourhood.getSucIp();}
    		
    		//if my suc suc id is < than my suc id, my suc suc is the largest id on the overlay and responsible
    		else if(Integer.parseInt(Neighbourhood.getSucSucId()) < Integer.parseInt(Neighbourhood.getSucId()))
    		{response = "THISNODERESPONSIBLE " + Neighbourhood.getSucIp();}
    		
    		else if (Integer.parseInt(Neighbourhood.getSucSucId()) >= Integer.parseInt(key_part))
    		{response = "THISNODERESPONSIBLE " + Neighbourhood.getSucSucIp();}
    			
    		else {response = "ASK " + Neighbourhood.getSucSucIp();}
    	}
    	
    	else if(command_part.equals("STOREKEY"))
    	{
    		if (Integer.parseInt(Neighbourhood.getMyId()) >= Integer.parseInt(key_part) && !(Integer.parseInt(Neighbourhood.getPreId()) >= Integer.parseInt(key_part)))
    		{
    			DHT.addToDHT(key_part, ip_part);
    			response = "ACK";
    		}
    		else 
    		{response = "ASK " + Neighbourhood.getSucSucIp();}
    	}
    	
    	else if(command_part.equals("NODELIST"))
    	{
    		response = "NODESWITHFILE " + DHT.getIpList(key_part).get(0);
    		
    		for(int i = 1; i < DHT.getIpList(key_part).size(); i++ )
    		{
    			response = response + " " + DHT.getIpList(key_part).get(i);
    		}
    	}
    		
    	//TODO:other reponses to queries for key responsibility
    	
    	else if(command_part.equals("REQUESTINDEXSOFHASH")) //greg
        {
            //TODO: from the OwnFiles object (or whatever), return the indices of the file segments that you posses 
            //for the file key_part 

        }

        else if(command_part.equals("REQUEST")) //greg
        {         
            //indexURL = "/" + filename + "/" + fileIndex //return the URL where the index can be retrieved
            ServeFileIndex(indexURL); //create the http server to serve the file index at the specific URL
            //response = "ACK PORTNUM" + indexURL;
            //TODO index URL real thing
        }

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
                if(command_part.equals("REQUEST")) //Greg
                {
                    hash_part = temp[1];
                    index_part = temp[2];
                    break;
                }
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


    private void ServeFileIndex(String indexDir) //greg
    {
        HttpServer server = null;
		try 
		{
			server = HttpServer.create(new InetSocketAddress(Neighbourhood.getMyIp(), Neighbourhood.getPort()), 0);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        server.createContext(indexDir, new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler //greg
    {
            public void handle(HttpExchange t) throws IOException 
        {
                    String response = "";//assign the index of the file here, as a byte
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
        }
    }   
    
}