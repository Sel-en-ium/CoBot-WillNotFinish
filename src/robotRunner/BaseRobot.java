package robotRunner;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class BaseRobot extends Robot
{
	public BaseRobot() throws AWTException {
		super();
	}

	public void key(int keyEvent) {
		this.keyPress(keyEvent);
		this.keyRelease(keyEvent);
	}

	public void leftClick(int x, int y) {
		this.mouseMove(x, y);
		this.mousePress(InputEvent.BUTTON1_MASK);
		this.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	public void rightClick(int x, int y) {
		//int buttons = MouseInfo.getNumberOfButtons();
		this.mouseMove(x, y);
		this.mousePress(InputEvent.BUTTON3_MASK);
		this.mouseRelease(InputEvent.BUTTON3_MASK);
	}

	public boolean compareImages(Rectangle currentRect, String baseFile) throws Exception {
		BufferedImage current = this.createScreenCapture(currentRect);
		BufferedImage base = ImageIO.read(new File("BaseScreens/" + baseFile));
		
		//ImageIO.write(current, "png", new File("newscreen.png"));
		
		int width = current.getWidth();
		int height = current.getHeight();
		
		if (width > base.getWidth() || height > base.getHeight()) {
			throw new Exception("Error image is bigger than the base image.");
		}
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++)
			{
				if (base.getRGB(x, y) != current.getRGB(x, y)) {
					return false;
				}
			}
		}
		return true;
	}

}
