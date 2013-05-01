import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

// Added a comment
public class Client implements Runnable
{
	
	Thread t;
	private CheckAlive neighbourhoodWatch; // greg
	Socket socket = null;
	PrintWriter sender = null;
	BufferedReader receiver = null;
	ClientProtocol protocol = new ClientProtocol();
		
	public Client()
	{
		t = new Thread(this);
		t.start();
	}
	
	public void run() 
	{ 

		//Join Overlay
		String IP = protocol.getSuperNodeIP();
		
		while (!IP.equalsIgnoreCase(null)) // while we have not yet found a slot
		{		
			//Create socket to talk to server
			try 
			{
				socket = new Socket(IP, 4017);//make a new connection (first and 2nd time is with supernode)
				sender = new PrintWriter(socket.getOutputStream(), true);
		        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}

	        sender.println( protocol.sendJoin() );//the protocol decides which message to send within 'join algorithm'
	        
	        try 
	        {
				IP = protocol.receiveJoin( receiver.readLine() );//the protocol returns the next IP to talk to
			} 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
	        
		}	
		// TODO Tell peeps you're in the hood

		
		//Joined overlay, watch neighbours
		neighbourhoodWatch = new CheckAlive();//greg
		
		
		// TODO Carry on with calls the client must make
		// 
		//
		//
		//
		// ETAI code for distributing file keys
		//TODO hash the files that you own
		//TODO loop this function for as many files as you have
		//TODO a check to redistribute file keys. This includes toggling a flag after a download completes 
		protocol.distributeFileKey("", socket);
		
		//TODO Prompted by the user who desires downloading a specific key
		protocol.RetreiveFileKeyList("", socket);
		
		
		
		
	}
	
}


