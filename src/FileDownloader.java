import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
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
		boolean fileComplete = false;
		int IP_number = 0;
		List<Integer> indices = null;
		while(!fileComplete)
		{
			//Request the indexes of the file that the IPs have in sequential order. Download the ones that 
			//we do not yet have. Stay with the same IP for the multiple files we have.
			
			try 
			{
				socket = new Socket(key_list.get(IP_number), 4017);
				sender = new PrintWriter(socket.getOutputStream(), true);
		        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}

	        sender.println( protocol.getIndexQuery(hash) );//the protocol decides which message to send within 'join algorithm'
	        
	        try 
	        {
				indices = protocol.processIndexQuery( receiver.readLine() );//the protocol returns the next IP to talk to
			} 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
	        

	        //Find which of the indices we dont have from this IP. 
	        if (we dont have one)
	        	
	        	URL website = new URL("http://www.website.com/information.asp");
	        	ReadableByteChannel rbc = Channels.newChannel(website.openStream());
	       		FileOutputStream fos = new FileOutputStream("information.html");
	       		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	       
	       		
	       		keep the same Ip
	        else
	        	IP_number++
	        	
	        if(we have all)
	        	fileComplete = true;
	      
	        
		}
		
		
		
	}


	


}