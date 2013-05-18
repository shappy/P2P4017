import java.util.ArrayList;
import java.util.List;
/*
 *Stores a list of OwnFiles that I own which in turn contains all the needed info about those files
 */

//Ariel

public class OwnFileList 
{
	
	// data member that holds our list of own files
	private static ArrayList<OwnFile> own_file_list = new ArrayList<OwnFile>();

	
	
	
	// Add a new file to your list
	public static synchronized void addFile(String fileKey)
	{
		OwnFile own_file = new OwnFile(fileKey);
		own_file_list.add(own_file);
	}
	
	public static synchronized void addIndex(String fileKey, Integer index)
	{
		own_file_list.get( indexOfList(fileKey) ).addIndex(index);
	}
	
	public static synchronized void setHasBeenDistributed(String fileKey)
	{
		own_file_list.get( indexOfList(fileKey) ).setHasBeenDistributed();
	}
	
	

	// Retrieve the list of indices that you own of a certain file key
	public static synchronized ArrayList<Integer> getIndexList(String fileKey)
	{ 
		return own_file_list.get( indexOfList(fileKey) ).getIndices();
	}
	
	public static synchronized int getNumberOfIndices(String fileKey)
	{ 
		return own_file_list.get( indexOfList(fileKey) ).getNumberIndices();
	}
	
	public static synchronized boolean isOwned(String fileKey, int index)
	{ 		
		return own_file_list.get( indexOfList(fileKey) ).getIndices().contains(index) ;
	}
	
	public static void setNumberIndices(String fileKey, int numberOfIndices) 
	{
		own_file_list.get( indexOfList(fileKey) ).setNumberIndices(numberOfIndices);
	}
	
	
	
	
	
	private static int indexOfList(String fileKey)
	{
		//get the OwnFile that is described by this key
		int i = 0;
		for (i = 0; i< own_file_list.size(); i++)
		{
			if ( own_file_list.get(i).getKey().equals(fileKey));
			break;
		}	
		return i;
	}



	
}


