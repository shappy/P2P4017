import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

//Ariel
public class FileDownloader implements Runnable 
{
	//Data Members
	Thread t;
	List<String> ip_list = new ArrayList<String>();
	String key = "";
	
	//private Neighbourhood neighbours;
	private ClientProtocol protocol;
	private Socket socket = null;
	private PrintWriter sender = null;
	private BufferedReader receiver = null;

	//Functions
	public FileDownloader(String key, List<String> ip_list)
	{
		t = new Thread(this); //Assign this object to its own thread
		this.ip_list = ip_list;
		this.key = key;
		t.start();
	}

	public void run()
	{
		boolean isFileComplete = false;
		int IP_index = 0;
		List<Integer> indicesInfo = new ArrayList<Integer>();
		boolean firstRequest = true;
		int numberOfIndices = 0;
		int numberOwnedByIP = 0;
		
		OwnFileList.addFile(key);// add the key to the list although it doesnt contain anything yet

		
		while(!isFileComplete)
		{
			//Request the indexes of the file that the IPs have in sequential order. Download the ones that 
			//we do not yet have. Stay with the same IP for the multiple files they have.
			
			try 
			{
				socket = new Socket(ip_list.get(IP_index), Neighbourhood.getPort());
				sender = new PrintWriter(socket.getOutputStream(), true);
		        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}

	        sender.println( protocol.getIndexQuery(key) );//The server is asked for which indices of this hash that they have
	        
	        try 
	        {
				indicesInfo = protocol.processIndexResponse( receiver.readLine() );//indices Info now holds #indices and then indices it owns
			} 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
	        
	        //If this is the first time we are asking for a part of this file, save the number of indices it consists of
	        if (firstRequest)
	        {
	        	firstRequest = false;
		        OwnFileList.setNumberIndices(key, indicesInfo.get(0));
	        }
	        
	        //Number of indices owned by that IP
	        numberOwnedByIP = indicesInfo.size() - 1;//-1 bc dont include the number of indices info
	        
	        for (int i=0; i<numberOwnedByIP; i++)
	        {
	        	String data;
	        	if (!OwnFileList.isOwned(key, indicesInfo.get(i+1) ) ) //If we don't own it
	        	{
	        		//add that index to the OwnFile object, covert to int
	        		data = download(indicesInfo.get(i+1), ip_list.get(IP_index));//download that index
	        		OwnFileList.addIndex(key, indicesInfo.get(i+1) );
	        	}
	        	
	        }
	        
    		IP_index++;// go to the next IP address if we have downloaded all the indices we need from this IP
    		
    		if (OwnFileList.isComplete(key))
	        {
	        	System.out.println("The file was downloaded successfuly");
	        	isFileComplete = true;
	        }
    		else if (IP_index == ip_list.size())//if we have checked all of the IP addresses (note we start at 0)
	        {
	        	System.out.println("The entire file was not downloaded due to the parts not existing");
	        	isFileComplete = true;
	        }
	        
	        

	      
	     }
	     
		
	}
	
	public String download(int index, String ip)
	{
		String indexDirectory = "";
		String downloadedIndex;
		String data = "";

		try 
		{
			socket = new Socket(ip, Neighbourhood.getPort());
			sender = new PrintWriter(socket.getOutputStream(), true);
	        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
        sender.println( protocol.requestIndex(key, index) );
        
        try 
        {
        	indexDirectory = protocol.processRequestIndexResponse( receiver.readLine() );
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
		
        if (indexDirectory.equals("REJECT"))
        {
        	System.out.println("Server has rejected request to download index: " + Integer.toString(index));
        }
        else
        {

        	FileOutputStream fos;
    		try 
    		{
    			String dir = "http://" + ip + ":" + Neighbourhood.getPort() + indexDirectory;
    			URL url = new URL(dir); //TODO probably should actually return the URL!
    			URLConnection HttpConnection =  url.openConnection(); 
    			
    			BufferedReader in = new BufferedReader(
    					new InputStreamReader(
    							HttpConnection.getInputStream()));
    			
    			
    			while((data = in.readLine()) != null)
    			{
    			   in.close();
    			}
    	   		

    		} 
    		catch (FileNotFoundException e) 
    		{
            	System.out.println("Something went wrong in downloading index number" + Integer.toString(index));
    			e.printStackTrace();
    		} 
    		catch (IOException e) 
    		{
            	System.out.println("Something went wrong in downloading index number" + Integer.toString(index));
    			e.printStackTrace();
    		}
    		

        	
        }
		return data;	
		
		
	}
	


}