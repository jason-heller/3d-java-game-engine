package core;


import org.lwjgl.opengl.Display;

import audio.AudioHandler;
import dev.Console;
import gl.Render;
import gl.Window;
import io.Controls;
import io.Input;
import io.Settings;
import map.architecture.ArchitectureHandler;
import scene.MainMenu;
import scene.Scene;
import ui.UI;

public class Application {
	
	public static final String TITLE = "Granny Horror Game";
	public static final String VERSION = "Version 0.2.3pa"; // Alpha Version, Minor, revision/patch
	
	public static Scene scene;
	private static Class<?> nextScene;

	private static float tickTimer = 0f;
	private static boolean forceClose;
	public static boolean paused = false;
	public static final int TICKS_PER_SECOND = 25;
	public static final float TICKRATE = 1f / TICKS_PER_SECOND;
	
	public static void changeScene(Class<?> sceneClass) {
		scene.cleanUp();
		UI.clear();
		nextScene = sceneClass;
	}

	public static void close() {
		forceClose = true;
	}

	public static void main(String[] args) throws InterruptedException {
		//System.setProperty("org.lwjgl.librarypath", new File("lib/windows").getAbsolutePath());
		
		AudioHandler.init();
		Settings.init();
		Controls.init();
		Window.create();
		Render.init();
		Console.init();
		
		Window.update();
		
		scene = new MainMenu();

		for (final String arg : args) {
			Console.send(arg);
		}

		while ((!Display.isCloseRequested() && !forceClose)) {
			
			Window.update();
			UI.update();
			scene.update();
			
			Console.update();
			
			if (nextScene != null) {
				try {
					scene = (Scene) nextScene.newInstance();
				} catch (final InstantiationException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				}

				nextScene = null;
			} else {
				tickTimer += Window.deltaTime;
				if (tickTimer >= TICKRATE) {
					tickTimer -= TICKRATE;
					scene.tick();
					AudioHandler.update(scene.getCamera());
					
				}
				Input.poll();
				Render.renderPass(scene);

				Render.postRender(scene);
			}
		}

		scene.cleanUp();
		Render.cleanUp();

		// Thread.sleep(50);
		Resources.cleanUp();
		AudioHandler.cleanUp();
		Window.destroy();
		Settings.save();
		Controls.save();
		
		Thread.sleep(1000);
		System.exit(0);
	}
	
	public static boolean isCloseRequested() {
		return forceClose;
	}
}
