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

//Shappy
public class FileDownloader implements Runnable 
{
	//Data Members
	Thread t;
	List<String> key_list = null;
	String key = "";
	
	//private Neighbourhood neighbours;
	private ClientProtocol protocol;
	private Socket socket = null;
	private PrintWriter sender = null;
	private BufferedReader receiver = null;

	//Functions
	public FileDownloader(String key, List<String> key_list)
	{
		t = new Thread(this); //Assign this object to its own thread
		this.key_list = key_list;
		this.key = key;
		t.start();
	}

	public void run()
	{
		boolean isFileComplete = false;
		int IP_index = 0;
		
		List<Integer> own_indices = new ArrayList<Integer>();//TODO get own indices here
		List<Integer> indicesInfo = new ArrayList<Integer>();
		
		while(!isFileComplete)
		{
			//Request the indexes of the file that the IPs have in sequential order. Download the ones that 
			//we do not yet have. Stay with the same IP for the multiple files they have.
			
			try 
			{
				socket = new Socket(key_list.get(IP_index), Neighbourhood.getPort());
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
	        

	        //Find which of the indices we don't have from this IP.
	        //Need the list of indices for each file that we own.
	        int numberOfIndices = indicesInfo.get(0);
	        //TODO save this value if our one doesnt know yet
	        
	        int size = indicesInfo.size() - 1;//-1 bc dont include the number of indices info
	        
	        for (int i=0; i<size; i++)
	        {
	        	if (!own_indices.contains( indicesInfo.get(i) ) ) //If we don't own it
	        		download(i, key_list.get(IP_index));//download that index
	        }
	        
    		IP_index++;// go to the next IP address if we have downloaded all the indices we need
    		
	        if (IP_index == key_list.size())//if we have checked all of the IP addresses (note we start at 0)
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
        sender.println( protocol.getPortNum(key, index) );
        
        try 
        {
			port = protocol.processPortResponse( receiver.readLine() );//the protocol returns the next IP to talk to
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
		
        if (port.equals("REJECT"))
        	System.out.println("Server has rejected request to download index: " + Integer.toString(index));

		
	
   		FileOutputStream fos;
		try 
		{
			URL url = new URL(port); //TODO probably should actually return the URL!
	    	ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			fos = new FileOutputStream(key  + ":" + Integer.toString(index));
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