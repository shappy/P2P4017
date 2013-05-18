import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.security.*;
// Added a comment
public class Client implements Runnable
{
	
	Thread t;
	//private CheckAlive neighbourhoodWatch; // greg
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
		
		while (!IP.equalsIgnoreCase("done") && !Neighbourhood.isSuperNode()) // while we have not yet found a slot
		{		
			//Create socket to talk to server
			try 
			{
				socket = new Socket(IP, Neighbourhood.getPort());//make a new connection (first and 2nd time is with supernode)
				sender = new PrintWriter(socket.getOutputStream(), true);
		        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("My client is sending: " + protocol.sendJoin());
	        sender.println( protocol.sendJoin() );//the protocol decides which message to send within 'join algorithm'
	        
	        try 
	        {
	        	String response = receiver.readLine();
				//IP = protocol.receiveJoin( receiver.readLine() );//the protocol returns the next IP to talk to
	        	IP = protocol.receiveJoin(response);
	        	System.out.println("My client is receiving: " + response);
	        } 
	        catch (IOException e) 
	        {
				e.printStackTrace();
			}
	        
		}	
		
		//Tell peeps you're in the hood
		if (!Neighbourhood.isSuperNode())
		protocol.updateNeighbourhood();
		
		//Check that it is a sufficiently sized overlay
		while(!protocol.isSufficientForOverlay())
		{
			try 
			{
				System.out.println("Waiting for sufficient overlay for 1 second");
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		
				
		//Update your own prepre and sucsuc before checking alive
		protocol.updateMyNeighbourhood();
		
		System.out.println("before distribute");
		
		//Seeing if need to wait for all to update neighbourhood.
		try 
		{
			Thread.sleep(1000);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		// ETAI code for distributing file keys
		//TODO hash the files that you own
		//TODO a check to redistribute file keys. This includes toggling a flag after a download completes 
		//List<String> list = new ArrayList<String>();
		//String hash1 = hashFile("file1");
		//list.add(hash1);
		try {
			protocol.distributeFileKeys(socket);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //changed function to accept a List<String>, use accordingly
		
		System.out.println("Just before check alive");
		//Joined overlay, now watch neighbours
		new CheckAlive();//greg

		
		//Code to wait for user input on what file he/she wants to download
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String key ="";  
		List<String> ip_list = null;
		FileDownloader file_downloader = null;

		while(!key.equalsIgnoreCase("exit"))
		{
			System.out.println("Key of File to Download (exit to quit overlay): ");
			try 
			{
				key = input.readLine();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			protocol.retrieveFileKeyList(key, socket);
			ip_list = protocol.getKeyList();
			file_downloader = new FileDownloader(key, ip_list);//send the key list and the files key to be downloaded
		}
		
		
		
		
		//Close the socket and readers
		try 
		{
			receiver.close();
			sender.close();
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}	
	}
}


