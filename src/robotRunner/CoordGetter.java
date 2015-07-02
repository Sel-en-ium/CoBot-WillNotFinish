package robotRunner;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.MouseInfo;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

 

public class CoordGetter
{
	private PointerInfo pointerInfo;
	
	public CoordGetter()
	{
		pointerInfo = MouseInfo.getPointerInfo();
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
