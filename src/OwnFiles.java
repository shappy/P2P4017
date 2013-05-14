//import java.util.ArrayList;
//import java.util.List;
///*
// *Stores a 2d array with the key, number of indices of this file, array of indices owned
// */
//
////Ariel
//
//public class OwnFiles {
//
//	
//	private static ArrayList< ArrayList<Integer>> ownFileTable = null;
//	private static ArrayList<Integer> keyLookup = null;
//	private static ArrayList<Integer> temp = null;
//
//	
//	public static synchronized List<Integer> getIndexList(String fileKey)
//	{ 
//		//get the inner list indexed for the specific fileKey 
//		ArrayList<Integer> info = ownFileTable.get(keyLookup.indexOf(Integer.getInteger(fileKey)));
//		info.remove(0);//remove the 
//		return info;
//	}
//	
//	public static synchronized boolean isOwned(String fileKey, int index)
//	{ 
//		//get the inner list indexed for the specific fileKey 
//		temp = ownFileTable.get(keyLookup.indexOf(Integer.getInteger(fileKey))) ;
//	}
//	
//	public static synchronized Integer getNumberOfIndices(String fileKey)
//	{ 
//		//get the inner list indexed for the specific fileKey 
//		temp = ownFileTable.get(keyLookup.indexOf(Integer.getInteger(fileKey)));
//		return info.get(0);
//	}
//	
//	public static synchronized void addToDHT(String fileKey, String ip)//add the ip associated with the fileKey
//	{
//		//separate message
//		if(hashLookup.contains(fileKey))
//		{
//			ipTable.get(hashLookup.indexOf(fileKey)).add(ip);
//		}
//		else
//		{
//			hashLookup.add(fileKey);
//			ArrayList<String> temp = new ArrayList<String>();
//			temp.add(ip);
//			ipTable.add(hashLookup.indexOf(fileKey), temp);
//		}
//	}
//	
//	/*public static void main(String[] args) 
//	{
//		// TODO Auto-generated method stub
//
//	}
//*/
//}
