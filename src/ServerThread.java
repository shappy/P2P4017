import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable{

	private Socket client_socket;
	Thread t;
	
	public ServerThread(Socket socket)
	{
		client_socket = socket;
		t = new Thread(this);
		t.start();
	}
	
	public void run() 
	{
		try 
		{
			//Create listeners/senders and the protocol
			PrintWriter sender = new PrintWriter(client_socket.getOutputStream(), true);
            BufferedReader receiver = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            String requestFromClient, responseToClient;
            ServerProtocol protocol = new ServerProtocol();
            
            //Send the welcome message
            sender.println(protocol.firstServerMessage());
           
             //Listen to what the client says
            requestFromClient = receiver.readLine();
            responseToClient = protocol.respond(requestFromClient);
            System.out.println("The server is sending: " + responseToClient);
            
            //Respond to the client
            sender.println(responseToClient);
            
           //End all communication
            sender.close();
            receiver.close();
            client_socket.close();
            System.out.println("The server thread has completed communication with client");
        } 
		catch (IOException e) 
		{
            System.out.println("Something went wrong in communicating with client");
            e.printStackTrace();
        }
		
		
	}
	
	

	

	

}
