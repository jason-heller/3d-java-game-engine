package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import dev.cmd.Console;

public class Controls {

	public static File controlsFile = new File(FileUtils.SETTINGS_FOLDER + "/controls.ini");

	public static Map<String, Integer> controls = new LinkedHashMap<String, Integer>();
	public static Map<Integer, String> customBinds = new LinkedHashMap<Integer, String>();

	public static void bind(String keyName, String cmd) {
		for (final String id : controls.keySet()) {
			if (id.equals(keyName.toLowerCase())) {
				controls.put(id, Keyboard.getKeyIndex(cmd.toUpperCase()));
				return;
			}
		}

		final int key = Keyboard.getKeyIndex(keyName.toUpperCase());
		if (key == Keyboard.KEY_NONE) {
			return;
		}
		customBinds.put(key, cmd);
		save();
	}

	public static void defaults() {
		controls.clear();

		controls.put("pause", Keyboard.KEY_ESCAPE);
		controls.put("use_left", Input.KEY_LMB);
		controls.put("use_right", Input.KEY_RMB);
		
		controls.put("up", Keyboard.KEY_UP);
		controls.put("left", Keyboard.KEY_LEFT);
		controls.put("down", Keyboard.KEY_DOWN);
		controls.put("right", Keyboard.KEY_RIGHT);
	
		controls.put("tr_ollie", Keyboard.KEY_SPACE);
		controls.put("tr_flip", Keyboard.KEY_Z);
		controls.put("tr_grind", Keyboard.KEY_X);
		controls.put("tr_air", Keyboard.KEY_C);
		
		controls.put("tr_modifier_l", Keyboard.KEY_LCONTROL);
		controls.put("tr_modifier_r", Keyboard.KEY_RCONTROL);
	}

	public static int get(String id) {
		return controls.get(id);
	}

	public static void handleCustomBinds(int input) {
		if (Console.isVisible()) return;
		
		for (final int key : customBinds.keySet()) {
			if (input != key) {
				continue;
			}

			final String[] cmds = customBinds.get(key).split(";");
			for (String s : cmds) {
				s = s.replaceAll("\'", "\"");
				Console.send(s);
			}

		}
	}

	public static void init() {
		defaults();
		if (Settings.configFile.exists()) {
			load();
		}
	}

	public static void load() {
		try (BufferedReader br = new BufferedReader(new FileReader(controlsFile))) {
			for (String line; (line = br.readLine()) != null;) {
				final String[] data = line.split("=");
				if (data[0].contains("custom")) {
					final int key = Keyboard.getKeyIndex(data[0].split(":")[1]);
					customBinds.put(key, data[1]);
				} else {
					if (controls.containsKey(data[0])) {
						controls.put(data[0], Integer.parseInt(data[1]));
					}
				}
			}
		} catch (final FileNotFoundException e) {
			return;
		} catch (final IOException e) {
			Console.warning("malformatted controls file.");
		}
	}

	public static void save() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(controlsFile, false))) {
			for (final String line : controls.keySet()) {
				bw.write(line + "=" + controls.get(line) + "\n");
			}
			for (final Integer key : customBinds.keySet()) {
				bw.write("custom:" + Keyboard.getKeyName(key) + "=" + customBinds.get(key) + "\n");
			}
		} catch (final IOException e) {
			Console.warning("malformatted controls file.");
		}
	}

	public static void set(String id, int key) {
		controls.put(id, key);
	}

	public static int size() {
		return controls.keySet().size();
	}

	public static void unbind(String keyName) {
		final int key = Keyboard.getKeyIndex(keyName.toUpperCase());
		
		/*for (final String id : controls.keySet()) {
			if (controls.get(id).equals(key)) {
				controls.remove(id);
			}
		}*/
		
		customBinds.remove(key);
		save();
	}
}
