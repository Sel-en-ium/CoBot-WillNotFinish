package GameOCR;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class OCRChar
{	
	
	private static String[] specialNames = {
           "BackwardSlash",
           "ForwardSlash"
	};
	
	private static char[] specialChars = {
           '\\',
           '/'
	};
	
	BufferedImage image;
	char charName;
	
	public OCRChar(BufferedImage image, char charName) {
		this.image = image;
		this.charName = charName;
	}

	public OCRChar(String baseFolder, String fileName) throws IOException {
		this.image = ImageIO.read(new File(baseFolder + "/" + fileName));
		this.charName = fileName.charAt(0);
	}
	
	public OCRChar(String baseFolder, String folder, String fileName) throws IOException {
		this.image = ImageIO.read(new File(baseFolder + "/" + folder + "/" + fileName));
		
		if (folder.equals("Special")) {
			this.charName = translateSpecialName(fileName);
		}
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
	
	public int getStartHeight() throws IOException {
		int height = this.image.getHeight();
		
		for (int y = 0; y < height; y++) {
			if (this.image.getRGB(0, y) != new Color(255, 255, 255).getRGB()) {
				return y;
			}
		}
		throw new IOException("Could not find start pixel of " + this.charName);
	}
}
