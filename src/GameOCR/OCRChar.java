package GameOCR;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class OCRChar
{	
	/* Constants */
	
	private static int BLACK = -16777216; // Non interfering character pixel
	private static int WHITE = -1; // Non interfering whitespace
	private static int RED = -1237980; // Interfering character pixel
	private static int GREY = -3947581; // Interfering whitespace
	
	
	private static String[] specialNames = {
           "BackwardSlash",
           "ForwardSlash"
	};
	
	private static char[] specialChars = {
           '\\',
           '/'
	};
	
	/* Class Properties */
	
	BufferedImage image;
	char charName;
	int imageWidth;
	int imageHeight;
	int xLeftTopPixel;
	int yLeftTopPixel;
	int nonInterferingZoneLeft; // Inclusive
	int nonInterferingZoneRight; // Exclusive (aka, pixel where interfering starts [or the width])
	int numCharacterPixels;

	public OCRChar(String baseFolder, String fileName) throws Exception {
		this.image = ImageIO.read(new File(baseFolder + "/" + fileName));
		this.charName = fileName.charAt(0);
		this.imageWidth = image.getWidth();
		this.imageHeight = image.getHeight();
		this.setNonInterferingZone();
		this.setLeftTopPixel();
		this.setNumCharacterPixels();
	}
	
	public OCRChar(String baseFolder, String folder, String fileName) throws Exception {
		this.image = ImageIO.read(new File(baseFolder + "/" + folder + "/" + fileName));
		
		if (folder.equals("Special")) {
			this.charName = translateSpecialName(fileName);
		}
		
		this.imageWidth = image.getWidth();
		this.imageHeight = image.getHeight();
		this.setNonInterferingZone();
		this.setLeftTopPixel();
		this.setNumCharacterPixels();
	}
	
	public char translateSpecialName(String fileName) throws IOException {
		String name = fileName.substring(0, fileName.indexOf('.'));
		
		for (int i = 0; i < specialNames.length; i++) {
			if (name.equals(specialNames[i])) {
				return specialChars[i];
			}
		}
		throw new IOException("No special name found for fileName: " + fileName);
	}
	
	private void setNonInterferingZone() {
		int width = this.image.getWidth();
		
		int x = 0;
		while (isInterferingPixel(x, 0) && x < width) {
			x++;
		}
		this.nonInterferingZoneLeft = x;
		
		x = width;
		while (isInterferingPixel(x-1, 0) && x-1 > 0) {
			x--;
		}
		this.nonInterferingZoneRight = x;
	}
	
	private void setLeftTopPixel() throws Exception {
		
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				if (isCharacterPixel(x, y)) {
					this.xLeftTopPixel = x;
					this.yLeftTopPixel = y;
					return;
				}
			}
		}
		
		throw new Exception("Could not find start pixel of " + this.charName);
	}

	private void setNumCharacterPixels() {
		int num = 0;
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				if (isCharacterPixel(x, y)) {
					num++;
				}
			}
		}
		this.numCharacterPixels = num;
	}
	
	public boolean isCharacterPixel(int x, int y) {
		int pixelColor = this.image.getRGB(x, y);
		if (pixelColor == BLACK || pixelColor == RED) {
			return true;
		}
		return false;
	}
	
	public boolean isInterferingPixel(int x, int y) {
		int pixelColor = this.image.getRGB(x, y);
		if (pixelColor == RED || pixelColor == GREY) {
			return true;
		}
		return false;
	}
	
}
