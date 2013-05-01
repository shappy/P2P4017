import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.security.*;
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
	
	public String hashFile(String fileName)//string: fileName converted by SHA1 to string of hex
	{
		MessageDigest sha1 = null;
		FileInputStream fis = null;
		try 
		{
			sha1 = MessageDigest.getInstance("SHA1");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try 
		{
			fis = new FileInputStream(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] data = new byte[1024];
        int read = 0; 
        try 
        {
			while ((read = fis.read(data)) != -1) 
			{
			    sha1.update(data, 0, read);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		byte[] hashBytes = sha1.digest();
		StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) 
        {
          buffer.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        try 
        {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        String fileHash = buffer.toString();
        return fileHash;
	}
	
}


