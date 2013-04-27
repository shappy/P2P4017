import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable
{
	
	Thread t;
	//Thread checkAlive; //will run in the background to see who is alive and updating the neighbourhood
		
	public Client()
	{
		t = new Thread(this);
		t.start();
	}
	
	public void run() 
	{ 
		Socket socket = null;
		PrintWriter sender = null;
		BufferedReader receiver = null;
		ClientProtocol protocol = new ClientProtocol();  
		
		//Join Overlay
		boolean isOnOverlay = false;
		
		while (!isOnOverlay)
		{		
			//Create socket to talk to server
			try 
			{
				socket = new Socket(protocol.getSuperNode(), 4017);
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
			
	        String requestToServer = null; 
	        String responseFromServer = null;
	        
	        //Talk first
	       // sender.println(Integer.toString(i));
	        
	        try 
	        {
				responseFromServer = receiver.readLine();
			} 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
	        
	        //Receive communication from protocol and send to server
	        System.out.println("The server sent: " + responseFromServer);
	        //requestToServer = protocol.respond(responseFromServer);
	        System.out.println( "The client is sending: " + requestToServer);
	        sender.println(requestToServer);
		}	
	}
}


