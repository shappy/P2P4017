import java.util.ArrayList;
import java.util.List;
/*
 *Stores ip addresses in a 2d array of lists referenced by a lookup table of hashes. 
 */

//Etai
public class DHT {

	
	private static ArrayList< ArrayList<String>> ipTable = null;//Table of ip lists indexed according to whether
														   //associate with the same file key. The index is
														   //maintained externally by the lookup table
	private static ArrayList<String> hashLookup = null;//Lookup table used as reference for ip lists.
	
	public static synchronized List<String> getIpList(String fileKey)//Returns ip addresses for a given fileKey
	{ 
		//get the inner list indexed for the specific fileKey 
		List<String> inner = ipTable.get(hashLookup.indexOf(fileKey));
		
		return inner;
	}
	
	public static synchronized void addToDHT(String fileKey, String ip)//add the ip associated with the fileKey
	{
		//separate message
		if(hashLookup.contains(fileKey))
		{
			ipTable.get(hashLookup.indexOf(fileKey)).add(ip);
		}
		else
		{
			hashLookup.add(fileKey);
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(ip);
			ipTable.add(hashLookup.indexOf(fileKey), temp);
		}
	}
	
	/*public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}
*/
}
