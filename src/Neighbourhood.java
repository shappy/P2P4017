import java.util.ArrayList;
import java.util.List;

public class Neighbourhood
{
	//Data members
	// i think these must all be keys(ariel)
	private static String myId;
	private static String myIp;
	
	private static String sucId;
	private static String sucIp;
	
	private static String sucSucId;
	private static String sucSucIp;
	
	private static String preId;
	private static String preIp;
	
	private static String prePreId;
	private static String prePreIp;
	
	private static ArrayList< List<String>> keyHolderIpTable = null;
	private static ArrayList<String> keyHolderLookup = null;
	
	private static String swarmSize;
	
	private final String superNodeIP = "lkjlk"; //node to contact first 

	//Functions
	public static synchronized String getMyId() {
		return myId;
	}

	public static synchronized void setMyId(String myId) {
		Neighbourhood.myId = myId;
	}

	public static synchronized String getMyIp() {
		return myIp;
	}

	public static synchronized void setMyIp(String myIp) {
		Neighbourhood.myIp = myIp;
	}

	public synchronized static String getSucId() {
		return sucId;
	}

	public static synchronized void setSucId(String sucId) {
		Neighbourhood.sucId = sucId;
	}

	public static synchronized String getSucIp() {
		return sucIp;
	}

	public static synchronized void setSucIp(String sucIp) {
		Neighbourhood.sucIp = sucIp;
	}

	public static synchronized String getSucSucId() {
		return sucSucId;
	}

	public static synchronized void setSucSucId(String sucSucId) {
		Neighbourhood.sucSucId = sucSucId;
	}

	public static synchronized String getSucSucIp() {
		return sucSucIp;
	}

	public static synchronized void setSucSucIp(String sucSucIp) {
		Neighbourhood.sucSucIp = sucSucIp;
	}

	public static synchronized String getPreId() {
		return preId;
	}

	public static synchronized void setPreId(String preId) {
		Neighbourhood.preId = preId;
	}

	public static synchronized String getPreIp() {
		return preIp;
	}

	public static synchronized void setPreIp(String preIp) {
		Neighbourhood.preIp = preIp;
	}

	public static synchronized String getPrePreId() {
		return prePreId;
	}

	public static synchronized void setPrePreId(String prePreId) {
		Neighbourhood.prePreId = prePreId;
	}

	public static synchronized String getPrePreIp() {
		return prePreIp;
	}

	public static synchronized void setPrePreIp(String prePreIp) {
		Neighbourhood.prePreIp = prePreIp;
	}
	
	@SuppressWarnings("null")
	public static synchronized void addToKeyHolderList(String fileKey, String ip)
	{
		if(keyHolderLookup.contains(fileKey))
		{
			keyHolderIpTable.get(keyHolderLookup.indexOf(fileKey)).add(ip);
		}
		else
		{
			keyHolderLookup.add(fileKey);
			List<String> temp = null;
			temp.add(ip);
			keyHolderIpTable.add(keyHolderLookup.indexOf(fileKey),temp);
		}
	}
	
	public static synchronized List<String> getKeyHolderList(String fileKey)
	{
		List<String> ipList = keyHolderIpTable.get(keyHolderLookup.indexOf(fileKey));
		
		return ipList;
	}

	public static synchronized String getSwarmSize() {
		return swarmSize;
	}

	public static synchronized void setSwarmSize(String swarmSize) {
		Neighbourhood.swarmSize = swarmSize;
	}

	public String getSuperNodeIP() {
		return superNodeIP;
	}
	

}
