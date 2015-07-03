package GameOCR;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.List;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;


public class OCRReader
{	
	public static void main(String[] args) {
		try {
			OCRReader reader = new OCRReader("OcrTrainingImages", 11);
			BufferedImage image = ImageIO.read(new File("screenshot.png"));
			String readin = reader.parseImage(image);
			System.out.println("SUCCESS LOL!  Well here it is.. '" + readin + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int[] textColors = {
			-1, // White
			-10582070, // Met Blue
			-1571587, // Elite/Item bottom line Pink
			-16711936, // Equipment Bonus Green
			-65536, // Lock Text Red 
			-3866137, // Equipment Bonus purple
			-1317505, // Super Gold/Yellow
			-494583, // Tortoise Gem Bonus Orange
			-7549187, // Magic Def% Bonus Blue
			-8521341, // Sockected Gem Green
			-256, // Npc Yellow
			-32704 // Gourd kills Orange
	};
	
	LinkedList<LinkedList<OCRChar>> charMap;
	
	public OCRReader(String baseFolder, int maxHeight) throws IOException {
		this.charMap = new LinkedList<LinkedList<OCRChar>>();
		for (int i = 0; i < maxHeight; i++) {
			this.charMap.push(new LinkedList<OCRChar>());
		}
		
		OCRChar c;
		
		// Read in all characters
    	DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(baseFolder));
    	
        //iterates through all files in the directory
        for (Path currentFile: stream) {
            
        	if (Files.isDirectory(currentFile)) {
        		String folder = currentFile.getFileName().toString();
        		DirectoryStream<Path> subStream = Files.newDirectoryStream(currentFile);
        		for (Path subFile: subStream) {
        			c = new OCRChar(baseFolder, folder, subFile.getFileName().toString());
        			charMap.get(c.getStartHeight()).push(c);
        		}
        	} else {
        		c = new OCRChar(baseFolder, currentFile.getFileName().toString());
        		charMap.get(c.getStartHeight()).push(c);
        	}
        }
	}
	
	public String parseImage(BufferedImage image) {
		int textColor;
		int topLine;
		
		try {
			textColor = getFirstColor(image, 0);
			topLine = getTopLine(image, textColor);
		} catch (Exception e) {
			
			String fileName = "Errors/ColorOrTopLine" + new Date().getTime() + ".png";
			try {
				ImageIO.write(image, "png", new File(fileName));
			} catch (IOException ioe) {
				System.err.println("Error saving error output (lol): "+ ioe);
			}
			System.err.println(e + "  Saved image to " + fileName + ".");
			return "";
		}		
		
		try {
			return getChar(image, textColor, 1, 1, topLine, true) + "";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "fail";
		}
	}
	
	private int getTopLine(BufferedImage image, int textColor) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				// Found a text pixel, try to match it
				if (image.getRGB(x, y) == textColor) {
					for (int a = 0; a < charMap.size(); a++) {
						try {
							OCRChar match = findMatch(image, textColor, x, y, a);
							return y - match.getStartHeight();
						} catch (Exception e) {
							
						}
					}
				}
			}
		}
		
		throw new Exception("Could not find the top line.");
	}
	
	private int getFirstColor(BufferedImage image, int startY) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();
		
		for (int y = startY; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matchesTextColor(image.getRGB(x, y))) {
					return image.getRGB(x, y);
				}
			}
		}
		
		throw new Exception("Could not find any known text color.");
	}
	
	private boolean matchesTextColor(int color) {
		for (int i = 0; i < textColors.length; i++) {
			if (color == textColors[i]) {
				return true;
			}
		}
		return false;
	}
	
	private char getChar(BufferedImage image, int textColor, int xStart, int yStart, int offset, boolean erase) throws Exception {
		return findMatch(image, textColor, xStart, yStart, offset).charName;
	}
	
	/**
	 * 
	 * @param image
	 * @param textColor
	 * @param xStart
	 * @param yStart
	 * @param offset Number of pixels down from top line
	 * @return
	 * @throws Exception
	 */
	private OCRChar findMatch(BufferedImage image, int textColor, int xStart, int yStart, int offset) throws Exception {
		LinkedList<OCRChar> list = charMap.get(offset);
		
		for (int a = 0; a < list.size(); a++) {
			if (testMatch(list.get(a).image, image, textColor, xStart, yStart, offset)) {
				return list.get(a);
			}
		}
		
		throw new Exception("Could not match a char with offset " + offset + " to the given pixel.");
	}
	
	private boolean testMatch(BufferedImage icon, BufferedImage image, int textColor, int xStart, int yStart, int offset) {
		int width = icon.getWidth();
		int height = icon.getHeight();
		
		if (width + xStart > image.getWidth() || height + yStart - offset> image.getHeight()) {
			return false;  // icon doesn't fit
		}
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (icon.getRGB(x, y) != Color.WHITE.getRGB()) {
					if (image.getRGB(x + xStart, y + yStart) != textColor) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
