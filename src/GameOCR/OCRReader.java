package GameOCR;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class OCRReader {
	public static void main(String[] args) {
		try {
			OCRReader reader = new OCRReader("OcrTrainingImages", 11);
			reader.printCharMap();
			BufferedImage image = ImageIO.read(new File("screenshot.png"));
			String readin = reader.readLines(image);
			if (DEBUG) {
				ImageIO.write(image, "png", new File("Errors/Processed.png"));
			}
			System.out.println("SUCCESS LOL!  Well here it is.. '" + readin + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean DEBUG = false;

	private static int nonTextColor = -16777215; // Just off black, RGB = 0,0,1
	private static int[] textColors = { -1, // White
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

	// Leftmost cols first
	private static int[][] generalInterferenceZoneLeft = { { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	// Rightmost cols first
	private static int[][] generalInterferenceZoneRight = { { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 } };

	private LinkedList<LinkedList<OCRChar>> charMap;
	private int maxHeight;
	private BufferedImage image;

	public OCRReader(String baseFolder, int maxHeight) throws IOException {
		this.maxHeight = maxHeight;
		this.charMap = new LinkedList<LinkedList<OCRChar>>();
		for (int i = 0; i < maxHeight; i++) {
			this.charMap.push(new LinkedList<OCRChar>());
		}

		// Iterates through all files in the directory
		DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(baseFolder));
		for (Path currentFile : stream) {

			if (Files.isDirectory(currentFile)) {

				String folder = currentFile.getFileName().toString();
				DirectoryStream<Path> subStream = Files.newDirectoryStream(currentFile);
				for (Path subFile : subStream) {
					addToCharMap(baseFolder, folder, subFile.getFileName().toString());
				}
			} else {
				addToCharMap(baseFolder, "", currentFile.getFileName().toString());
			}
		}
	}

	private void addToCharMap(String baseFolder, String folder, String fileName) {
		try {
			OCRChar c;
			if (folder.equals("")) {
				c = new OCRChar(baseFolder, fileName);
			} else {
				c = new OCRChar(baseFolder, folder, fileName);
			}

			// Insert char in order of largest numPixels first
			List<OCRChar> list = charMap.get(c.yLeftTopPixel);
			int i = 0;
			while (i < list.size()) {
				if (list.get(i).numCharacterPixels > c.numCharacterPixels) {
					i++;
				} else {
					break;
				}
			}
			list.add(i, c);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String readLines(BufferedImage image) {
		this.image = image;
		return readLine(0);
	}

	public String readLine(BufferedImage image) {
		this.image = image;
		return readLine(0);
	}

	private String readLine(int topLineSearchStart) {
		int textColor;
		int topLine;

		try {
			textColor = getFirstColor(topLineSearchStart);
			topLine = getNextTopLine(textColor, topLineSearchStart);
			System.out.println("textColor: " + textColor);
			System.out.println("TopLine: " + topLine);

		} catch (Exception e) {
			String fileName = "Errors/ColorOrTopLine" + new Date().getTime() + ".png";
			try {
				ImageIO.write(image, "png", new File(fileName));
			} catch (IOException ioe) {
				System.err.println("Error saving error output (lol): " + ioe);
			}
			System.err.println(e + "  Saved image to " + fileName + ".");
			return "";
		}

		String result = "";
		int width = image.getWidth();

		for (int x = 0; x < width; x++) {
			for (int y = topLine; y < topLine + maxHeight; y++) {
				if (image.getRGB(x, y) == textColor) {
					try {
						result += getChar(textColor, x, y, y - topLine, true);
					} catch (Exception e) {

					}
				}
			}
		}
		return result;
	}

	private int getNextTopLine(int textColor, int yStart) throws Exception {
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
							OCRChar match = findMatch(textColor, x, y, a);
							return y - match.yLeftTopPixel;
						} catch (Exception e) {

						}
					}
				}
			}
		}

		throw new Exception("Could not find the top line.");
	}

	private int getFirstColor(int startY) throws Exception {
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
	 * Returns the character match of a suspected pixel at a certain offset.
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
	private char getChar(int textColor, int xStart, int yStart, int offset, boolean erase) throws Exception {
		OCRChar match = findMatch(textColor, xStart, yStart, offset);

		if (erase) {
			eraseCharAt(match, xStart, yStart, offset);
		}

		return match.charName;
	}

	/**
	 * Iterates through all characters with a certain offset until a match is
	 * found.
	 * 
	 * @param image
	 * @param textColor
	 * @param xStart
	 * @param yStart
	 * @param offset
	 *            Number of pixels down from top line
	 * @return
	 * @throws Exception
	 */
	private OCRChar findMatch(int textColor, int xStart, int yStart, int offset) throws Exception {
		LinkedList<OCRChar> list = charMap.get(offset);

		for (int a = 0; a < list.size(); a++) {
			if (testMatch(list.get(a), textColor, xStart, yStart)) {
				return list.get(a);
			}
		}

		throw new Exception("Could not match a char with offset " + offset + " to the given pixel.");
	}

	/**
	 * Decides whether a given charIcon aligns with an expected character
	 * location.
	 * 
	 * @param charIcon
	 * @param image
	 * @param textColor
	 * @param xStart
	 * @param yStart
	 * @param offset
	 * @return
	 */
	private boolean testMatch(OCRChar chara, int textColor, int xStart, int yStart) {
		int xImg = xStart - chara.xLeftTopPixel;
		int yImg = yStart - chara.yLeftTopPixel;

		if (chara.imageWidth + xImg > image.getWidth() || chara.imageHeight + yImg > image.getHeight()) {
			return false; // icon doesn't fit
		}

		for (int x = 0; x < chara.imageWidth; x++) {
			for (int y = 0; y < chara.imageHeight; y++) {

				// Character pixels should have text color in image
				if (chara.isCharacterPixel(x, y)) {
					if (image.getRGB(xImg + x, yImg + y) != textColor) {
						return false;
					}
					// And non-interfering, non-character pixels should not have
					// text color in image
				} else if (!isInterferencePixel(chara, x, y)) {
					if (image.getRGB(xImg + x, yImg + y) == textColor) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Sets all matched character pixels to an off black color that hopefully is
	 * not a textColor.
	 * 
	 * @param charIcon
	 * @param image
	 * @param xStart
	 * @param yStart
	 * @param offset
	 */
	private void eraseCharAt(OCRChar chara, int xStart, int yStart, int offset) {
		int xImg = xStart - chara.xLeftTopPixel;
		int yImg = yStart - chara.yLeftTopPixel;

		for (int x = chara.nonInterferingZoneLeft; x < chara.nonInterferingZoneRight; x++) {
			for (int y = 0; y < chara.imageHeight; y++) {
				if (chara.isCharacterPixel(x, y) && !isRightInterferencePixel(chara, x, y)) {
					image.setRGB(xImg + x, yImg + y, nonTextColor);
				}
			}
		}
		try {
			if (DEBUG) {
				ImageIO.write(image, "png", new File("Errors/erasred" + chara.charName + ".png"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isInterferencePixel(OCRChar chara, int x, int y) {
		// In own interference zone
		if (this.isLeftInterferencePixel(chara, x, y) || this.isRightInterferencePixel(chara, x, y)) {
			return true;
		}
		return false;
	}

	private boolean isLeftInterferencePixel(OCRChar chara, int x, int y) {
		// In own interference zone
		if (x < chara.nonInterferingZoneLeft) {
			return true;
		}

		// In general interference zone
		int colsFromLeftOfInterferenceZone = x - chara.nonInterferingZoneLeft;
		if (colsFromLeftOfInterferenceZone < generalInterferenceZoneLeft.length
				&& generalInterferenceZoneLeft[colsFromLeftOfInterferenceZone][y] == 1) {
			return true;
		}

		return false;
	}

	private boolean isRightInterferencePixel(OCRChar chara, int x, int y) {
		// In own interference zone
		if (x >= chara.nonInterferingZoneRight) {
			return true;
		}

		// In general interference zone
		int colsFromRightOfInterferenceZone = chara.nonInterferingZoneRight - x - 1;
		if (colsFromRightOfInterferenceZone < generalInterferenceZoneRight.length
				&& generalInterferenceZoneRight[colsFromRightOfInterferenceZone][y] == 1) {
			return true;
		}

		return false;
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
