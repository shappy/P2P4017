
public class Info {
	private static int a = 0;
	
	public static synchronized int getA()
	{
		return a;
	}
	
	public static synchronized void setA(int number)
	{
		a = number;
	}

}
