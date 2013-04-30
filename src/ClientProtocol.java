public class ClientProtocol 
{	
	
	public String checkAliveQuery()
	{
		return "ALIVE";
	}

	public String checkAliveResponse()
	{	
		return "ACK";
	}
	
	public String getPredecessorQuery()
	{
		
		return "PREDECESSORSKEY";
	}
    
	public String getPredecessorResponse()
	{
		return "RETPREDECESSORSKEY";
	}
	
	public String getSuccessorQuery()
	{
		return "SUCCESSORSKEY";
	}
	
	public String getSuccessorResponse()
	{
		return "RETSUCCESSORSKEY";
	}
	
}