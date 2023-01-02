package util;

import java.util.ArrayList;
import java.util.List;

import ui.Character;
import ui.Font;

public class StrUtils {
	public static float getWidth(String text, float textSize) {
		return getWidth(Font.defaultFont, text, textSize);
	}
	
	public static float getWidth(Font font, String text, float textSize) {
		return getWidth(font, text, Float.POSITIVE_INFINITY, textSize);
	}
	
	public static float getWidth(Font font, String text, float lineWidth, float textSize) {
		return getBounds(font, text, lineWidth, textSize)[0];
	}
	
	public static float getHeight(String text, float textSize) {
		return getHeight(Font.defaultFont, text, textSize);
	}
	
	public static float getHeight(Font font, String text, float textSize) {
		return getHeight(font, text, Float.POSITIVE_INFINITY, textSize);
	}
	
	public static float getHeight(Font font, String text, float lineWidth, float textSize) {
		return getBounds(font, text, lineWidth, textSize)[1];
	}
	
	public static float[] getBounds(Font font, String text, float lineWidth, float textSize) {
		float dx = 0;
		float dy = 0;
		float width = 0;
		float height = 0;

		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);

			if (c == '#') {
				i += 1;
			} else if (c == '\n') {
				dx = 0;
				dy += font.getCharacter('A').getyOffset() * (textSize + 1);
			} else if (c >= 32 && c <= 126) {
				final Character character = font.getCharacter(c);

				dx += character.getxAdvance() * textSize;
				width = Math.max(dx, width);
				height = Math.max(dy, height);

				if (dx > lineWidth && c == ' ') {
					dx = 0;
					dy += font.getCharacter('|').getyOffset() + font.getPaddingHeight();
				}
			}
		}
		
		return new float[] {width, height};
	}
	
	public static String[] splitByWidth(Font font, String text, float lineWidth, float textSize) {
		float dx = 0;
		List<String> strs = new ArrayList<>();
		char lastColor = ' ';
		String currentLine = "";
		int len = text.length();
		for (int i = 0; i < len; i++) {
			final char c = text.charAt(i);

			if (c == '#' && i != len - 1) {
				i += 1;
				lastColor = text.charAt(i);
				currentLine += "#" + lastColor;
			} else if (c == '\n') {
				dx = 0;
				strs.add(currentLine);
				currentLine = lastColor == ' ' ? "" : "#" + lastColor;
			} else if (c >= 32 && c <= 126) {
				final Character character = font.getCharacter(c);

				dx += character.getxAdvance() * textSize;
				if (dx >= lineWidth) {
					strs.add(currentLine);
					currentLine = lastColor == ' ' ? "" : "#" + lastColor;
					dx = 0;
				}
				currentLine += c;
				
			}
		}
		
		if (currentLine.length() > 0)
			strs.add(currentLine);
		
		String[] strsArray = new String[strs.size()];
		for(int i = 0; i < strsArray.length; i++)
			strsArray[i] = strs.get(i);
		
		return strsArray;
	}
	
	public static String[] parseCommand(String string) {
		String read = "";
		final List<String> getArgs = new ArrayList<String>();
		boolean insideQuotes = false;
		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);

			if (c == '\"') {
				insideQuotes = !insideQuotes;

				if (!insideQuotes) {
					getArgs.add(read);
					read = "";
				}
			}

			else if (c == ' ' && !insideQuotes) {
				if (read.length() > 0) {
					getArgs.add(read);
				}
				read = "";
			}

			else {
				read += c;
			}
		}

		if (read.length() > 0) {
			getArgs.add(read);
		}

		String[] strs = new String[getArgs.size()];
		strs = getArgs.toArray(strs);
		return strs;
	}

	public static String[] argsOnly(String[] strs) {
		String[] args = new String[strs.length - 1];
		if (strs.length > 1) {
			for (int i = 1; i < strs.length; i++) {
				args[i - 1] = strs[i];
			}
		}
		return args;
	}
}
