package dev.cmd;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import core.App;
import dev.Debug;
import io.Input;
import ui.Font;
import ui.Image;
import ui.UI;
import util.Colors;
import util.StrUtils;

public class Console {
	private static final int VISIBLE_LINES = 32, MAX_LINES = 200;
	private static int lineCopyInd = -1;

	private static int x = 20, y = 20;

	private static final int BORDER_WIDTH = 2;
	private static final int HEADER_HEIGHT = 20;
	private static final int WIDTH = 640;

	private static List<String> log = new ArrayList<String>();
	private static List<String> predictions = new ArrayList<String>();
	private static Image backdrop = new Image("none", 0, 0);
	private static Image border = new Image("none", 0, 0);

	private static boolean visible = false;
	private static boolean blockComment = false;
	private static String input = "";

	private static boolean drag = false;
	private static int dragX = 0, lastX = 0;
	private static int dragY = 0, lastY = 0;

	private static final float FONT_SIZE = .14f;
	private static final int FONT_HEIGHT = (int) (22 * (FONT_SIZE / .3f));

	private static final Vector3f BACKGROUND_COLOR = Colors.BLACK;
	private static final Vector3f BORDER_COLOR = Colors.GUI_BORDER_COLOR;
	private static final int MAX_PREDICTIONS = 12;

	private static int lineViewInd = 0;
	
	private static DecimalFormat df = new DecimalFormat("0.0000");
	public static boolean isBlocking = true;

	public static void clear() {
		log.clear();
	}

	public static void doSceneTick() {
		if (visible) {
			visible = false;
			App.scene.update();
			visible = true;
		}
	}

	public static boolean isVisible() {
		return visible;
	}

	public static void warning(Object... x) {
		for (Object obj : x) {
			log("#yWarning: " + obj);
		}
	}

	public static void severe(Object... objects) {
		for (Object obj : objects) {
			log("#rSevere: " + obj);
			System.err.println(obj.toString());
		}
	}

	public static void highlight(Object... x) {
		for (Object obj : x) {
			log("#b" + obj);
		}
	}

	public static void log(Object... x) {
		String s = "";

		for (int i = 0; i < x.length; i++) {
			if (x[i] == null) {
				s += "null";
			} else {
				if (x[i] instanceof Float) {
					s += df.format(x[i]);
				} else {
					s += x[i].toString();
				}
			}
			
			s += (i == x.length - 1 ? "" : ", ");
		}

		log(s);
	}

	public static void log(String text) {
		final String[] lines = StrUtils.splitByWidth(Font.defaultFont, text, WIDTH, FONT_SIZE);
		
		for (final String line : lines) {
			log.add(line);
			
			if (log.size() > MAX_LINES) {
				log.remove(0);
			}

			if (log.size() >= VISIBLE_LINES - 1 && log.size() < MAX_LINES) {
				lineViewInd++;
			}
		}
	}
	
	private static boolean mouseOver(int mx, int my) {
		return mx > x && my > y && mx < x + WIDTH && my < y + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT;
	}

	private static void predict(String input) {
		predictions.clear();
		if (input.equals("") || input.split(" ").length == 0) {
			return;
		}
		for (final CommandData commandData : CommandData.values()) {
			Command command = commandData.command;
			if (command.getName().indexOf(input.split(" ")[0]) == 0) {
				predictions.add(command.getName() + " " + command.getArgs());

				if (predictions.size() > MAX_PREDICTIONS) {
					return;
				}
			}
		}
	}

	public static void printStackTrace(Exception e) {
		System.err.println(e.toString());
		final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (final StackTraceElement element : stackTraceElements) {
			final String[] lines = element.toString().split("\r\n");
			for (final String line : lines) {
				if (line.length() != 0) {
					System.err.println(line);
				}
			}
		}
	}

	public static void send(String string) {
		String[] multiCmds = string.split("::");
		if (multiCmds.length > 1) {
			for(String cmd : multiCmds) {
				send(cmd);
			}
			return;
		}
		
		
		if (blockComment && string.contains("*/")) {
			blockComment = false;
			string = string.substring(string.indexOf("*/") + 2);
		}

		if (string.length() == 0) {
			return;
		}

		if (string.charAt(0) == '#' || blockComment) {
			return;
		}

		if (string.contains("/*")) {
			string = string.substring(0, string.indexOf("/*"));
			blockComment = true;
		}

		if (string.contains("//")) {
			string = string.substring(0, string.indexOf("//"));
		}

		if (string.length() == 0) {
			return;
		}

		// Get args
		String[] strs = StrUtils.parseCommand(string);

		if (strs == null || strs.length == 0) {
			return;
		}

		final String command = strs[0];
		final String[] args = StrUtils.argsOnly(strs);

		// Now check if you typed a command
		final Command cmd = CommandData.getCommand(command);
		if (cmd != null) {
			if (cmd.requiresCheats && Debug.debugMode || !cmd.requiresCheats) {
				cmd.execute(args);
			} else {
				log("Cheats must be enabled");
			}
			return;
		}

		log("No such command: " + command);
	}

	public static void toggle() {
		visible = !visible;
		lineCopyInd = -1;
		input = "";
		predictions.clear();

		if (visible) {
			Input.requestMouseRelease();
		} else {
			Input.requestMouseGrab();
		}
	}

	public static void update() {
		if (visible) {
			final char[] keysIn = Input.getTypedKey();

			backdrop.setColor(BACKGROUND_COLOR);
			backdrop.x = x + BORDER_WIDTH;
			backdrop.y = y + HEADER_HEIGHT;
			backdrop.w = WIDTH;
			backdrop.h = (VISIBLE_LINES + 1) * FONT_HEIGHT;
			backdrop.setDepth(-9998);

			border.setColor(BORDER_COLOR);
			border.x = x;
			border.y = y;
			border.w = WIDTH + BORDER_WIDTH * 2;
			border.h = BORDER_WIDTH * 2 + HEADER_HEIGHT + (VISIBLE_LINES + 1) * FONT_HEIGHT;
			border.setDepth(-9997);

			for (final char in : keysIn) {
				if (in != '`') {
					if (in == '\b') {
						if (input.length() > 0) {
							input = input.substring(0, input.length() - 1);
						}
					} else {
						input += in;
					}

					predict(input);
				}
			}

			if (Input.isPressed(Keyboard.KEY_DOWN)) {
				lineCopyInd = Math.min(lineCopyInd + 1, predictions.size() - 1);
				if (lineCopyInd >= 0) {
					String pred = predictions.get(lineCopyInd);
					input = pred.substring(0, pred.indexOf(' '));
				} else {
					while (lineCopyInd < 0 && log.size() != 0) {
						input = log.get(log.size() + lineCopyInd);
						if (input.length() > 0 && input.charAt(0) == ']' && input.length() > 1) {
							input = input.substring(1);
							predict(input);
							break;
						}

						lineCopyInd++;
					}

					if (lineCopyInd == 0) {
						input = "";
						predictions.clear();
					}
				}
			}
			
			if (Input.isPressed(Keyboard.KEY_TAB) && predictions.size() > 0) {
				String pred = predictions.get(0);
				input = pred.substring(0, pred.indexOf(' '));
			} 

			if (Input.isPressed(Keyboard.KEY_UP)) {
				final int originalInd = lineCopyInd;
				lineCopyInd = Math.max(lineCopyInd - 1, -log.size());
				if (lineCopyInd >= 0 && predictions.size() > 0) {
					String pred = predictions.get(lineCopyInd);
					input = pred.substring(0, pred.indexOf(' '));
				} else {
					String newInput = "";
					while (lineCopyInd > -log.size()) {
						newInput = log.get(log.size() + lineCopyInd);
						if (newInput.length() > 0 && newInput.charAt(0) == ']' && newInput.length() > 1) {
							break;
						}

						lineCopyInd--;
					}

					if (newInput.length() > 0 && newInput.charAt(0) == ']') {
						input = newInput.substring(1);
						predict(input);
					} else {
						lineCopyInd = originalInd;
					}
				}

			}

			if (Input.isPressed(Keyboard.KEY_HOME)) {
				lineViewInd = Math.max(log.size() - (VISIBLE_LINES - 1), 0);
			}

			if (Input.isPressed(Keyboard.KEY_END)) {
				lineViewInd = 0;
			}

			if (Input.isPressed(Keyboard.KEY_NEXT)) {
				lineViewInd = Math.min(lineViewInd + 8, Math.max(log.size() - (VISIBLE_LINES - 1), 0));
			}

			if (Input.isPressed(Keyboard.KEY_PRIOR)) {
				lineViewInd = Math.max(lineViewInd - 8, 0);
			}

			if (Input.isPressed(Keyboard.KEY_RETURN)) {
				log("]" + input);
				send(input);
				input = "";
				lineCopyInd = -1;
				predictions.clear();
			}

			UI.drawImage(border);
			UI.drawImage(backdrop);

			UI.drawString(Font.defaultFont, "Console", x + 3, y + 3, .16f, UI.width, false).setDepth(-Integer.MAX_VALUE);

			final int lineBottomViewInd = lineViewInd + VISIBLE_LINES - 1;
			for (int i = lineViewInd; i < log.size() && i < lineBottomViewInd; i++) {
				final int lineY = y + HEADER_HEIGHT + BORDER_WIDTH + (i - lineViewInd) * FONT_HEIGHT;
				UI.drawString(Font.defaultFont, log.get(i), x + BORDER_WIDTH * 2, lineY, FONT_SIZE, UI.width, false)
						.setDepth(-Integer.MAX_VALUE);
			}

			int predWidth = 16;
			for (int i = 0; i < predictions.size(); i++) {
				predWidth = Math.max(predWidth, (int) (predictions.get(i).length() * (16 * (FONT_SIZE / .3f))));
			}

			UI.drawRect(x, y + HEADER_HEIGHT + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT, predWidth,
					predictions.size() * FONT_HEIGHT + BORDER_WIDTH, BORDER_COLOR).setDepth(-9998);
			for (int i = 0; i < predictions.size(); i++) {
				final int lineY = (y + (VISIBLE_LINES + i + 3) * FONT_HEIGHT) + 1;

				final String color = lineCopyInd == i ? "#w" : "#6";

				UI.drawString(Font.defaultFont, color + predictions.get(i), x + BORDER_WIDTH * 2, lineY, FONT_SIZE,
						UI.width, false).setDepth(-Integer.MAX_VALUE);
			}

			final String blinker = System.currentTimeMillis() % 750 > 375 ? "|" : "";
			
			String[] inputTrunc = StrUtils.splitByWidth(Font.defaultFont, ">" + input, WIDTH-12, FONT_SIZE);
			String cont = (inputTrunc.length > 1) ? "..." : blinker;
			
			UI.drawString(Font.defaultFont, inputTrunc[0] + cont, x + BORDER_WIDTH * 2,
					y + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT, FONT_SIZE, UI.width, false).setDepth(-Integer.MAX_VALUE);
		}

		final int mx = Input.getMouseX();
		final int my = Input.getMouseY();
		if (mouseOver(mx, my)) {

			if (visible && Input.isPressed(Input.KEY_LMB) && !drag) {
				drag = true;
				dragX = mx;
				dragY = my;
				lastX = x;
				lastY = y;
			}

			final int wheel = -Input.getMouseDWheel();
			final int speed = 8;

			if (wheel < 0) {
				lineViewInd = Math.max(lineViewInd - speed, 0);
			}
			if (wheel > 0) {
				lineViewInd = Math.min(lineViewInd + speed, Math.max(log.size() - (VISIBLE_LINES - 1), 0));
			}
		}

		if (Input.isReleased(Input.KEY_LMB) && drag) {
			drag = false;
		}

		if (drag) {
			x = lastX + Input.getMouseX() - dragX;
			y = lastY + Input.getMouseY() - dragY;
		}

		if (Input.isPressed(Keyboard.KEY_GRAVE) && Debug.allowConsole) {
			toggle();
		}
		
		if (visible && Input.isPressed(Keyboard.KEY_ESCAPE)) {
			toggle();
		}
	}

	private static void predictEnum(String string, Set<String> values) {
		List<String> preds = new ArrayList<String>();
		
		if (string.equals("")) {
			for(String e : values) {
				preds.add(e.toLowerCase());
			}
		} else {
			for(String e : values) {
				String name = e.toLowerCase();
				
				if (name.indexOf(string) == 0) {
					preds.add(name);
				}
			}
		}
		
		int boxY = (y + HEADER_HEIGHT + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT) + predictions.size() * FONT_HEIGHT + BORDER_WIDTH;
		UI.drawRect(x+28, boxY, 70,
				preds.size()*10, BORDER_COLOR).setDepth(-9998);
		for (int i = 0; i < preds.size(); i++) {
			final int lineY = boxY + (i) * FONT_HEIGHT;

			UI.drawString(Font.defaultFont, "#s" + preds.get(i), x + 28 + BORDER_WIDTH * 2, lineY, FONT_SIZE,
					UI.width, false).setDepth(-Integer.MAX_VALUE);
		}
	}
}
