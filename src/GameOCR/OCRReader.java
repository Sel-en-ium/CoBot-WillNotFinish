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
			reader.printCharMap();
			BufferedImage image = ImageIO.read(new File("screenshot.png"));
			String readin = reader.readLines(image);
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
	
	private LinkedList<LinkedList<OCRChar>> charMap;
	private int maxHeight;
	
	public OCRReader(String baseFolder, int maxHeight) throws IOException {
		this.maxHeight = maxHeight;
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
	
	public String readLines(BufferedImage image) {
		return readLine(image);
	}
	
	public String readLine(BufferedImage image) {
		return readLine(image, 0);
	}
	
	public String readLine(BufferedImage image, int topLineSearchStart) {
		int textColor;
		int topLine;
		
		try {
			textColor = getFirstColor(image, 0);
			topLine = getNextTopLine(image, textColor, topLineSearchStart);
			System.out.println("textColor: " + textColor);
			System.out.println("TopLine: " + topLine);
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
		
		String result = "";
		int width = image.getWidth();
		int height = image.getHeight();
		
		for (int x = 0; x < width; x++) {
			for (int y = topLine; y < topLine + maxHeight; y++) {
				if (image.getRGB(x, y) == textColor) {
					try {
						result += getChar(image, textColor, x, y, y - topLine, true);
					} catch (Exception e) {
						
					}
				}
			}
		}
		return result;
	}
	
	private int getNextTopLine(BufferedImage image, int textColor, int yStart) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();
		
		if (yStart + maxHeight > height) {
			throw new Exception("TopLine: Did not try to find as there is not enough space remaing below y=" + yStart);
		}
		
		for (int y = yStart; y < height; y++) {
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
				if (matchesTextColors(image.getRGB(x, y))) {
					return image.getRGB(x, y);
				}
			}
		}
		
		throw new Exception("Could not find any known text color.");
	}
	
	/**
	 * Returns the character match of a suspected pixel at a ceratin offset.
	 * 
	 * @param image
	 * @param textColor
	 * @param xStart
	 * @param yStart
	 * @param offset
	 * @param erase
	 * @return
	 * @throws Exception
	 */
	private char getChar(BufferedImage image, int textColor, int xStart, int yStart, int offset, boolean erase) throws Exception {
		OCRChar match = findMatch(image, textColor, xStart, yStart, offset);
		
		if (erase) {
			eraseCharAt(match.image, image, xStart, yStart, offset);
		}
		
		return match.charName;
	}
	
	/**
	 * Iterates through all characters with a certain offset until a match is found.
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
	
	/**
	 * Decides whether a given charIcon aligns with an expected character location. 
	 * 
	 * @param charIcon
	 * @param image
	 * @param textColor
	 * @param xStart
	 * @param yStart
	 * @param offset
	 * @return
	 */
	private boolean testMatch(BufferedImage charIcon, BufferedImage image, int textColor, int xStart, int yStart, int offset) {
		int width = charIcon.getWidth();
		int height = charIcon.getHeight();
		
		if (width + xStart > image.getWidth() || height + yStart - offset> image.getHeight()) {
			return false;  // icon doesn't fit
		}
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (charIcon.getRGB(x, y) != Color.WHITE.getRGB()) {
					if (image.getRGB(x + xStart, y + yStart - offset) != textColor) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Sets all matched character pixels to an off black color that hopefully is not a textColor.
	 * 
	 * @param charIcon
	 * @param image
	 * @param xStart
	 * @param yStart
	 * @param offset
	 */
	private void eraseCharAt(BufferedImage charIcon, BufferedImage image, int xStart, int yStart, int offset) {
		int width = charIcon.getWidth();
		int height = charIcon.getHeight();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (charIcon.getRGB(x, y) == Color.BLACK.getRGB()) {
					image.setRGB(x + xStart, y + yStart - offset, new Color(0, 0, 1).getRGB());
				}
			}
		}
	}
	
	private boolean matchesTextColors(int color) {
		for (int i = 0; i < textColors.length; i++) {
			if (color == textColors[i]) {
				return true;
			}
		}
		return false;
	}
	
	private void printCharMap() {
		LinkedList<OCRChar> list;
		for (int a = 0; a < this.charMap.size(); a++) {
			list = this.charMap.get(a);
			System.out.print("Offset: " + a + " [");
			for (int b = 0; b < list.size(); b++) {
				if (b != 0) {
					System.out.print(",");
				}
				System.out.print(list.get(b).charName);
			}
			System.out.println("]");
		}
	}
}
