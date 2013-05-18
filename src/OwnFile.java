import java.util.ArrayList;
import java.util.List;

public class OwnFile 
{
	String key = "";
	int number_indices = 0;
	ArrayList<Integer> indices = new ArrayList<Integer>();
	boolean hasBeenDistributed = false;
	
	public OwnFile(String key)
	{
		this.key = key;
	}
	
	public String getKey()
	{
		return key;
	}
	
	public int getNumberIndices()
	{
		return number_indices;
	}
	
	public void setNumberIndices(int num)
	{
		this.number_indices = num;
	}
	
	public  ArrayList<Integer> getIndices()
	{
		return indices;
	}
	
	public void addIndex(Integer index)
	{
		indices.add(index);
	}
	
	public void setHasBeenDistributed()
	{
		hasBeenDistributed  = true;
	}

	public void addIntegerIndex(ArrayList<Integer> tempIntegers) 
	{
		this.indices = tempIntegers;
		
	}

}
