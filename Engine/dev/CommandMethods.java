package dev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import audio.AudioHandler;
import core.Application;
import gl.Camera;
import gl.Render;
import gl.Window;
import map.architecture.ArchitectureHandler;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.SpawnHandler;
import scene.entity.hostile.TestHostileEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;
import scene.singlearc.SingleArcScene;

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
	
	public static void log(String msg) {
		Console.log(msg);
	}
	
	public static void map(String map) {
		if (!ArchitectureHandler.isValidMap(map)) {
			Console.log("No such map: " + map);
			return;
		}
		AudioHandler.stopAll();
		PlayableScene.currentMap = map;
		Application.changeScene(SingleArcScene.class);
	}
	
	public static void nextmap() {
		ArchitectureHandler.nextMap();
	}
	
	public static void noclip() {
		if (Application.scene.getCamera().getControlStyle() == Camera.FIRST_PERSON) {
			Application.scene.getCamera().setControlStyle(Camera.SPECTATOR);
		} else {
			Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		}
	}
	
	public static void spawn(String name, String cmd) {
		PlayableScene PlayableScene;
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		} else {
			PlayableScene = (PlayableScene)Application.scene;
		}
		
		/*if (!cmd.contains("\"")) {
			Console.log("Usage: spawn <name> \"arg0 arg1 ... argn\"");
			return;
		}*/
		
		String parse = name + "\t" + cmd.replaceAll(" ", "\t");
		// HACK: Using \t allows name to have spaces
		String[] args = parse.split("\t");
		
		SpawnHandler.spawn(PlayableScene.getArcHandler().getArchitecture(), PlayableScene.getCamera(), args);
	}
	
	public static void kill() {
		PlayableScene PlayableScene;
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		} else {
			PlayableScene = (PlayableScene)Application.scene;
		}
		
		PlayableScene.getPlayer().takeDamage(PlayerEntity.getHp(0), 0);
	}
	
	public static void hurt(int damage, int part) {
		PlayableScene PlayableScene;
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		} else {
			PlayableScene = (PlayableScene)Application.scene;
		}
		
		if (damage < 0) {
			heal(-damage, part);
		} else {
			PlayableScene.getPlayer().takeDamage(damage, part);
		}
	}
	
	public static void look(float yaw, float pitch, float roll) {
		Camera camera = Application.scene.getCamera();
		camera.setYaw(yaw);
		camera.setPitch(pitch);
		camera.setRoll(roll);
	}
	
	public static void shake(float time, float intensity) {
		Camera camera = Application.scene.getCamera();
		camera.shake(time, intensity);
	}
	
	public static void heal(int hp, int part) {
		PlayableScene PlayableScene;
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		} else {
			PlayableScene = (PlayableScene)Application.scene;
		}
		
		if (hp < 0) {
			hurt(-hp, part);
		} else {
			PlayableScene.getPlayer().heal(hp, part);
		}
	}
	
	public static void has_walker() {
		PlayerHandler.hasWalker = !PlayerHandler.hasWalker;
	}
	
	public static void tp(String a, String b, String c, String d) {
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		PlayableScene playableScene = (PlayableScene)Application.scene;
		Entity entity = null;
		
		if (a.startsWith("@")) {
			entity = EntityHandler.getEntity(a.substring(1));
			
			if (entity == null) {
				Console.log("No such entity: \"" + a.substring(1) + "\"");
				return;
			}

			entity.pos.x = Float.parseFloat(b);
			entity.pos.y = Float.parseFloat(c);
			entity.pos.z = Float.parseFloat(d);
		} else {
			entity = playableScene.getPlayer();

			entity.pos.x = Float.parseFloat(a);
			entity.pos.y = Float.parseFloat(b);
			entity.pos.z = Float.parseFloat(c);
		}
		
		Camera camera = playableScene.getCamera();
		camera.setPosition(entity.pos);
		playableScene.getArcHandler().update(camera);
	}
	
	public static void tp_rel(String a, String b, String c, String d) {
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		PlayableScene playableScene = (PlayableScene)Application.scene;
		Entity entity = null;
		
		if (a.startsWith("@")) {
			entity = EntityHandler.getEntity(a.substring(1));
			
			if (entity == null) {
				Console.log("No such entity: \"" + a.substring(1) + "\"");
				return;
			}

			entity.pos.x += Float.parseFloat(b);
			entity.pos.y += Float.parseFloat(c);
			entity.pos.z += Float.parseFloat(d);
		} else {
			entity = playableScene.getPlayer();

			entity.pos.x += Float.parseFloat(a);
			entity.pos.y += Float.parseFloat(b);
			entity.pos.z += Float.parseFloat(c);
		}
		
		Camera camera = playableScene.getCamera();
		camera.setPosition(entity.pos);
		playableScene.getArcHandler().update(camera);
	}
	
	public static void shadow_quality(int quality) {
		Render.shadowQuality = quality;
		if (Application.scene instanceof PlayableScene) {
			PlayableScene s = (PlayableScene)Application.scene;
			s.getArcHandler().getArchitecture().getLightmap().setFiltering(Render.shadowQuality);
		}
	}
	
	public static void mipmap_bias(float bias) {
		Render.defaultBias = bias;
		if (Application.scene instanceof PlayableScene) {
			PlayableScene s = (PlayableScene)Application.scene;
			s.getArcHandler().getArchitecture().changeMipmapBias();
		}
	}
	
	public static void water_quality(int quality) {
		Render.setWaterQuality(quality);
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
		quit();
	}
	
	private static void incorrectParams(String cmd, String ... strings) {
		String s = "Usage: "+cmd+" ";
		for(int i = 0; i < strings.length; i++) {
			s += "<"+strings[i]+"> ";
		}
		Console.log(s);
	}
}
