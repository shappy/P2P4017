
 
public class ServerProtocol {
	
	private Neighbourhood neighbourhood;
	
	public ServerProtocol()
	{
		neighbourhood = new Neighbourhood();
	}
    
    public String firstServerMessage () 
    {
    	String message ="1";
		return message;
    	
    }
 
    public String respond(String input) 
    {
    	int intInput = Integer.parseInt(input);
    	
    	if (intInput == 5)
    		return neighbourhood.getMyId();
    	else
    	{
    		neighbourhood.setMyId(input);
    		return "3";
    	}
    	  	
    	//return null;
    }
    	
    	
//    	if (input.equals("1"))
//    		return "2";
//    	else if (input.equals("2"))
//    		return "3";
//    	else if (input.equals("3"))
//    		return "4";
//    	else if (input.equals("4"))
//    		return "5";
//    	else if (input.equals("5"))
//    		return "6";
    	
    	
    
}