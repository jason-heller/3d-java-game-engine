package dev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import gl.Camera;
import gl.Window;
import map.Enviroment;
import map.Terrain;
import map.weather.Weather;
import scene.entity.Entity;
import scene.entity.EntityControl;
import scene.entity.EntityData;
import scene.overworld.Overworld;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;

public class CommandMethods {
	public static void logMessage(String msg) {
		Console.log(msg);
	}
	
	public static void volume(float value) {
		AudioHandler.volume = value;
		AudioHandler.changeMasterVolume();

	}
	
	public static void run(String file) {
		try {
			List<String> lines = Files.readAllLines(new File("cmds/" + file + ".txt").toPath());
			for(String line : lines) {
				Console.send(line);
			}
		} catch (IOException e) {
			Console.log("Could not read file: "+file);
		}
	}
	
	public static void spawn(String name) {
		if (Application.scene instanceof Overworld) {
			Overworld scene = ((Overworld)Application.scene);
			Vector3f at = scene.getSelectionPoint();
			if (at == null) at = scene.getPlayer().position;
			
			int id = EntityData.getId(name + "Entity");
			
			if (id == 0) {
				Console.log("No such entity exists");
				return;
			}
			
			Entity entity = EntityData.instantiate(id);
			entity.position.set(at);
			EntityControl.addEntity(entity);
		}
	}
	
	public static void noclip() {
		if (Application.scene.getCamera().getControlStyle() == Camera.FIRST_PERSON) {
			Application.scene.getCamera().setControlStyle(Camera.SPECTATOR);
		} else {
			Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		}
	}
	
	public static void chunk_distance(int distance) {
		distance = Math.max(distance, 1);
		Terrain.size = distance;
		if (Application.scene instanceof Overworld) {
			Overworld scene = ((Overworld)Application.scene);
			scene.getEnviroment().reloadTerrain();
		}
	}
	
	public static void tp(float x, float y, float z) {
		if (Application.scene instanceof Overworld) {
			Overworld scene = ((Overworld)Application.scene);
			scene.getCamera().setPosition(new Vector3f(x,y,z));
			scene.getEnviroment().reposition((int)x, (int)z);
			scene.getEnviroment().tick(scene);
			scene.getPlayer().position.set(x,y,z);
		}
	}
	
	public static void time(String action, String value) {
		if (!(Application.scene instanceof Overworld)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		
		Overworld overworld = (Overworld) Application.scene;
		Enviroment e = overworld.getEnviroment();
		
		if (action.equals("")) {
			action = "set";
		}
		
		switch(value) {
		case "morning":
		case "dawn":
			value = Integer.toString(Enviroment.DAWN);
			break;
		case "day":
			value = Integer.toString(Enviroment.DAY);
			break;
		case "dusk":
			value = Integer.toString(Enviroment.DUSK);
			break;
		case "night":
			value = Integer.toString(Enviroment.NIGHT);
			break;
		}
		
		if (value.matches("^[a-zA-Z]*$")) {
			incorrectParams("time", "set/add/freeze", "value");
			return;
		}
		
		if (action.equals("set")) {
			e.setTime(Integer.parseInt(value));
		}
		else if (action.equals("add")) {
			e.setTime(e.getTime() + Integer.parseInt(value));
		}
		else if (action.equals("freeze")) {
			e.toggleTime();
		}
		else {
			incorrectParams("time", "set/add/freeze", "value");
		}
	}
	
	public static void weather(String action, float value) {
		if (!(Application.scene instanceof Overworld)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		
		Overworld overworld = (Overworld) Application.scene;
		Weather w = overworld.getEnviroment().getWeather();
		
		if (action.equals("set")) {
			w.setWeather(value);
		}
		else if (action.equals("add")) {
			w.setWeather(w.getWeatherCell() + value);
		}
		else if (action.equals("freeze")) {
			w.freeze = !w.freeze;
		}
		else if (action.equals("clear")) {
			w.setWeather(0f);
		}
		else {
			incorrectParams("time", "set/add/freeze", "value");
		}
	}
	
	public static void give(String item, int amount) {
		if (!(Application.scene instanceof Overworld)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		
		amount = Math.max(amount, 1);
		
		Overworld overworld = (Overworld) Application.scene;
		Inventory inv = overworld.getInventory();
		
		for(int i = 0; i < Item.values().length; i++) {
			Item current = Item.values()[i];
			if (current.getName().toLowerCase().equals(item)) {
				inv.addItem(current, amount);
				break;
			}
		}
	}
	
	public static void fov(int fov) {
		Camera.fov = fov;
		Application.scene.getCamera().updateProjection();
	}
	
	public static void fps(int fps) {
		Window.maxFramerate = fps;
	}

	public static void quit() {
		Application.close();
	}
	
	public static void exit() {
		Application.close();
	}
	
	private static void incorrectParams(String cmd, String ... strings) {
		String s = "Usage: "+cmd+" ";
		for(int i = 0; i < strings.length; i++) {
			s += "<"+strings[i]+"> ";
		}
		Console.log(s);
	}
}
