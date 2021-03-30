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
import scene.entity.hostile.TestHostileEntity;
import scene.entity.utility.PlayerEntity;
import scene.entity.utility.PlayerHandler;
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
	
	public static void noclip() {
		if (Application.scene.getCamera().getControlStyle() == Camera.FIRST_PERSON) {
			Application.scene.getCamera().setControlStyle(Camera.SPECTATOR);
		} else {
			Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		}
	}
	
	public static void spawn_monster(float x, float y, float z) {
		TestHostileEntity.spawnViaCommand(x, y ,z);
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
			PlayableScene.getPlayer().heal(hp, PlayerEntity.HP_ALL);
		}
	}
	
	public static void has_walker() {
		PlayerHandler.hasWalker = !PlayerHandler.hasWalker;
	}
	
	public static void tp(String _x, String _y, String _z) {
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		PlayableScene playableScene = (PlayableScene)Application.scene;
		PlayerEntity player = playableScene.getPlayer();

		player.pos.x = Float.parseFloat(_x);
		player.pos.y = Float.parseFloat(_y);
		player.pos.z = Float.parseFloat(_z);
		Camera camera = playableScene.getCamera();
		camera.setPosition(player.pos);
	}
	
	public static void tp_rel(String _x, String _y, String _z) {
		if (!(Application.scene instanceof PlayableScene)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		PlayableScene playableScene = (PlayableScene)Application.scene;
		PlayerEntity player = playableScene.getPlayer();

		if (_x.startsWith("-")) {
			player.pos.x -= Float.parseFloat(_x.substring(1));
		} else {
			player.pos.x += Float.parseFloat(_x);
		}
		
		if (_y.startsWith("-")) {
			player.pos.y -= Float.parseFloat(_y.substring(1));
		} else {
			player.pos.y += Float.parseFloat(_y);
		}
		
		if (_z.startsWith("-")) {
			player.pos.z -= Float.parseFloat(_z.substring(1));
		} else {
			player.pos.z += Float.parseFloat(_z);
		}
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
