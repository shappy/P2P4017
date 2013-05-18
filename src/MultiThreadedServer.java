import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Etai
public class MultiThreadedServer implements Runnable 
{
	
		private int server_port = 0;
		private ServerSocket server_socket = null;
		Thread t;
		
		public MultiThreadedServer(int port)
		{
			this.server_port = port;		
			t = new Thread(this);
			t.start();        
		}
		
		public void run() 
		{
			
	        //calls the function
	        openServerSocket();
	        
	        boolean flag = true;
	        while(flag)
	        {
	            Socket client_socket = null;
	            
	            //Listen for incoming client
	            try 
	            {
	                client_socket = this.server_socket.accept();
	                //System.out.println("Client has connected."); 
	            } 
	            catch (IOException e) 
	            {
	                throw new RuntimeException("Error accepting client connection", e);
	            }
	            
	            //Create new client-server conversation, on a thread
	            new ServerThread(client_socket);
	        }
	        try 
	        {
				server_socket.close();
		        //System.out.println("Server Stopped.");

			} catch (IOException e) 
			{
				e.printStackTrace();
			}
	    }

	    private void openServerSocket() 
	    {
	        try 
	        {
	        	//System.out.println("Opening server socket ... ");
	            server_socket = new ServerSocket(server_port);
	            //System.out.println("Server socket listening...");
	        } 
	        catch (IOException e) 
	        {
	            throw new RuntimeException("Cannot open port "+ server_port, e);
	        }
	    }	
}
