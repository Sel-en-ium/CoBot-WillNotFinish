package robotRunner;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;

 

public class ColorGetter
{
	private Robot robot;
	
	public ColorGetter() throws AWTException
	{
		MouseInfo.getPointerInfo();
		robot = new Robot();
	}
 
 	public static void main(String[] args) throws AWTException
	{
 		ColorGetter theGetter = new ColorGetter();
 		for (int i = 0; i < 10; i++)
 		{
 			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 			theGetter.printColor();
 		}
	}
   
	private void printColor()
	{
		Point a = MouseInfo.getPointerInfo().getLocation();
		Rectangle rect = new Rectangle((int)a.getX(), (int)a.getY(), 1, 1);
		BufferedImage image = this.robot.createScreenCapture(rect);
		
    	System.out.println(image.getRGB(0, 0));
	}
}