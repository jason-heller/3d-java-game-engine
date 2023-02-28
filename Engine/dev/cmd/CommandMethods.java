package dev.cmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import audio.AudioHandler;
import core.App;
import dev.Debug;
import dev.RailBuilder;
import gl.Camera;
import gl.Render;
import gl.Window;
import io.Input;
import map.architecture.ArchitectureHandler;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.SpawnHandler;
import scene.mapscene.MapScene;

public class CommandMethods {
	
	private static void incorrectParams(String cmd, String ... strings) {
		String s = "Usage: "+cmd+" ";
		for(int i = 0; i < strings.length; i++) {
			s += "<"+strings[i]+"> ";
		}
		Console.log(s);
	}
	
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
	
	public static void log(String msg) {
		Console.log(msg);
	}
	
	public static void map(String map) {
		if (!ArchitectureHandler.isValidMap(map)) {
			Console.warning("No such map: " + map);
			return;
		}
		
		if (App.scene instanceof MapScene) {
			Input.requestMouseRelease();
		}
		
		AudioHandler.stopAll();
		PlayableScene.currentMap = map;
		App.changeScene(MapScene.class);
	}
	
	public static void noclip() {
		if (App.scene.getCamera().getControlStyle() != Camera.SPECTATOR) {
			App.scene.getCamera().setControlStyle(Camera.SPECTATOR);
		} else {
			App.scene.getCamera().setControlStyle(Camera.THIRD_PERSON);
		}
	}
	
	public static void spawn(String name, String cmd) {
		PlayableScene PlayableScene;
		if (!(App.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		} else {
			PlayableScene = (PlayableScene)App.scene;
		}
		
		/*if (!cmd.contains("\"")) {
			Console.log("Usage: spawn <name> \"arg0 arg1 ... argn\"");
			return;
		}*/
		
		String parse = name + "\t" + cmd.replaceAll(" ", "\t");
		// HACK: Using \t allows name to have spaces
		String[] args = parse.split("\t");
		
		SpawnHandler.spawn(PlayableScene.getArchitecture(), PlayableScene.getCamera(), args);
	}
	
	public static void look(float yaw, float pitch, float roll) {
		Camera camera = App.scene.getCamera();
		camera.setYaw(yaw);
		camera.setPitch(pitch);
		camera.setRoll(roll);
	}
	
	public static void shake(float time, float intensity) {
		Camera camera = App.scene.getCamera();
		camera.shake(time, intensity);
	}
	
	public static void tp(String a, String b, String c, String d) {
		if (!(App.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		PlayableScene playableScene = (PlayableScene)App.scene;
		Entity entity = null;
		
		if (a.startsWith("@")) {
			entity = EntityHandler.getEntityByName(a.substring(1));
			
			if (entity == null) {
				Console.log("No such entity: \"" + a.substring(1) + "\"");
				return;
			}

			entity.position.x = Float.parseFloat(b);
			entity.position.y = Float.parseFloat(c);
			entity.position.z = Float.parseFloat(d);
		} else {
			entity = playableScene.getPlayer();

			entity.position.x = Float.parseFloat(a);
			entity.position.y = Float.parseFloat(b);
			entity.position.z = Float.parseFloat(c);
		}
		
		Camera camera = playableScene.getCamera();
		camera.getPosition().set(entity.position);
		playableScene.getArcHandler().update(camera);
	}
	
	public static void tp_rel(String a, String b, String c, String d) {
		if (!(App.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		PlayableScene playableScene = (PlayableScene)App.scene;
		Entity entity = null;
		
		if (a.startsWith("@")) {
			entity = EntityHandler.getEntityByName(a.substring(1));
			
			if (entity == null) {
				Console.log("No such entity: \"" + a.substring(1) + "\"");
				return;
			}

			entity.position.x += Float.parseFloat(b);
			entity.position.y += Float.parseFloat(c);
			entity.position.z += Float.parseFloat(d);
		} else {
			entity = playableScene.getPlayer();

			entity.position.x += Float.parseFloat(a);
			entity.position.y += Float.parseFloat(b);
			entity.position.z += Float.parseFloat(c);
		}
		
		Camera camera = playableScene.getCamera();
		camera.getPosition().set(entity.position);
		playableScene.getArcHandler().update(camera);
	}
	
	public static void shadow_quality(int quality) {
		Render.shadowQuality = quality;
		if (App.scene instanceof PlayableScene) {
			PlayableScene s = (PlayableScene)App.scene;
			s.getArchitecture().getLightmap().setFiltering(Render.shadowQuality);
		}
	}
	
	public static void mipmap_bias(float bias) {
		Render.defaultBias = bias;
		if (App.scene instanceof PlayableScene) {
			PlayableScene s = (PlayableScene)App.scene;
			s.getArcHandler().getArchitecture().changeMipmapBias();
		}
	}
	
	public static void water_quality(int quality) {
		Render.setWaterQuality(quality);
	}
	
	public static void fov(int fov) {
		Camera.fov = fov;
		App.scene.getCamera().updateProjection();
	}
	
	public static void fps(int fps) {
		Window.maxFramerate = fps;
	}
	
	public static void clear() {
		Console.clear();
	}

	public static void quit() {
		App.close();
	}
	
	public static void exit() {
		quit();
	}
	
	public static void raillist_build() {
		RailBuilder.buildRailList();
	}
	
	public static void switch_stance() {
		if (App.scene instanceof PlayableScene) {
			PlayableScene s = (PlayableScene)App.scene;
			s.getPlayer().setSwitch(!s.getPlayer().isSwitch());
		}
	}
	
	public static void rail_mode() {
		Debug.railMode = !Debug.railMode;
		
		if (Debug.railMode)
			RailBuilder.fillList();
		
		App.scene.getCamera().setControlStyle(Camera.SPECTATOR);
	}
}
