package robotRunner;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;


public class GameRobot extends BaseRobot
{	
	public GameRobot() throws AWTException {
		super();
	}

	public void switchWindow(int window) {
		  this.key(KeyEvent.VK_WINDOWS);
		  this.delay(500);
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
	
	public boolean screenNpcDialogueVisible() throws Exception {
		return this.compareImages(new Rectangle(244, 0, 12, 6), "NpcDialogue.png");
	}

}