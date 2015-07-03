package robotRunner;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.MouseInfo;

 

public class CoordGetter
{
	
	public CoordGetter()
	{
	}
 
 	public static void main(String[] args) throws AWTException
	{
 		CoordGetter theGetter = new CoordGetter();
 		for (int i = 0; i < 10; i++)
 		{
 			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 			theGetter.printCoords();
 		}
		
	}
   
	private void printCoords()
	{
		Point a = MouseInfo.getPointerInfo().getLocation();
    	System.out.println(a.getX() + ", " + a.getY());
	}

}
