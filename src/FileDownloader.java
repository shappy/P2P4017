import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;


public class FileDownloader implements Runnable 
{
	//Data Members
	Thread t;
	List<String> key_list = null;
	String hash = "";
	
	//private Neighbourhood neighbours;
	private ClientProtocol protocol;
	private Socket socket = null;
	private PrintWriter sender = null;
	private BufferedReader receiver = null;

	//Functions
	public FileDownloader(String hash, List<String> key_list)
	{
		t = new Thread(this); //Assign this object to its own thread
		this.key_list = key_list;
		this.hash = hash;
		t.start();
	}

	public void run()
	{
		boolean isFileComplete = false;
		int IP_index = 0;
		List<Integer> own_indices = new ArrayList<Integer>();//TODO get own indices here
		List<Integer> indices = new ArrayList<Integer>();
		while(!isFileComplete)
		{
			//Request the indexes of the file that the IPs have in sequential order. Download the ones that 
			//we do not yet have. Stay with the same IP for the multiple files we have.
			
			try 
			{
				socket = new Socket(key_list.get(IP_index), 4017);
				sender = new PrintWriter(socket.getOutputStream(), true);
		        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}

	        sender.println( protocol.getIndexQuery(hash) );//The server is asked for which indices of this hash that they have
	        
	        try 
	        {
				indices = protocol.processIndexResponse( receiver.readLine() );//the protocol returns the next IP to talk to
			} 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
	        

	        //Find which of the indices we don't have from this IP.
	        //Need the list of indices for each file that we own.
	        int size = indices.size();
	        for (int i=0; i<size;i++)
	        {
	        	if (!own_indices.contains(indices.get(i))) //If we don't own it
	        		download(i, key_list.get(IP_index));
	        }
	        
    		IP_index = IP_index + 1;// go to the next IP address
    		
	        if (IP_index == key_list.size()-1)//if we have checked all of the IP address
	        {
	        	System.out.println("The entire file was not downloaded due to the parts not existing");
	        	isFileComplete = true;
	        }
	        if (true)//TODO function of if we have all of them
	        {
	        	System.out.println("The file was downloaded successfuly");
	        	isFileComplete = true;
	        }

	      
	     }
	     
		
	}
	
	public void download(int index, String ip)
	{
		String port = "";
		
		try 
		{
			socket = new Socket(ip, 4017);
			sender = new PrintWriter(socket.getOutputStream(), true);
	        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
        sender.println( protocol.getPortNum(hash, index) );//The server is asked for which indices of this hash that they have
        
        try 
        {
			port = protocol.processPortResponse( receiver.readLine() );//the protocol returns the next IP to talk to
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
		
		
	
   		FileOutputStream fos;
		try 
		{
			URL url = new URL(ip + ":" + port); //TODO probably should actually return the URL!
	    	ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			fos = new FileOutputStream(hash + ":" + Integer.toString(index));
	   		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	   		//TODO tell the index keeping class that you have this index now
	   		//TODO create trigger to broadcoast the new acquisition to the world if the first of the new file (otherwise they will ask anyway)

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
	


}