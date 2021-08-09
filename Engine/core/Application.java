package core;


import java.io.File;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;

import audio.AudioHandler;
import audio.recognition.SpeechRecognizer;
import dev.Console;
import gl.Render;
import gl.Window;
import io.Controls;
import io.Input;
import io.Settings;
import scene.MainMenu;
import scene.Scene;
import scene.entity.EntityHandler;
import ui.UI;

public class Application {
	
	public static final String TITLE = "Ghost Hunting Game (Demo)";
	public static final String VERSION = "Version 0.1.1pa"; // Alpha Version, Minor, revision/patch
	
	public static Scene scene;
	private static Class<?> nextScene;

	private static float tickTimer = 0f;
	private static boolean forceClose;
	public static boolean paused = false;
	
	public static final int TICKS_PER_SECOND = 25;
	public static final float TICKRATE = 1f / TICKS_PER_SECOND;
	
	public static String operatingSystem;
	public static String osArchitecture;
	public static String nativesPath;
	
	//private static long lastGC = 0;
	
	public static void changeScene(Class<?> sceneClass) {
		EntityHandler.clear();
		scene.cleanUp();
		UI.clear();
		nextScene = sceneClass;
	}

	public static void close() {
		forceClose = true;
	}

	public static void main(String[] args) throws InterruptedException {
		//System.setProperty("org.lwjgl.librarypath", new File("lib/windows").getAbsolutePath());
		initOpenGL();
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
		
		Console.send("run autoexec");
		
		SpeechRecognizer.init();

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
			}
			
			/*long time = System.currentTimeMillis();
			if (time - lastGC > 10000) {
				lastGC = time;
				System.gc();
			}*/
		}

		scene.cleanUp();
		Render.cleanUp();

		SpeechRecognizer.cleanUp();
		
		// Thread.sleep(50);
		Resources.cleanUp();
		AudioHandler.cleanUp();
		Window.destroy();
		Settings.save();
		Controls.save();
		
		Thread.sleep(1000);
		System.exit(0);
	}

	private static void initOpenGL() {
		Locale.setDefault(Locale.US);
		
		operatingSystem = System.getProperty("os.name").toLowerCase();
		osArchitecture = System.getProperty("os.arch").toLowerCase();
		nativesPath =  new File("lib").getAbsolutePath();
		
		System.out.println("operating system: " + operatingSystem);
		System.out.println("architecture: " + osArchitecture);
		
		String ext = "";
		if (operatingSystem.contains("win")) {
			ext = "windows";
		} else if (operatingSystem.contains("lin")) {
			ext = "linux";
		} else if (operatingSystem.contains("mac")) {
			ext = "macosx";
		} else if (operatingSystem.contains("sol")) {
			ext = "solaris";
		} else {
			JOptionPane.showMessageDialog(null, "Unsupported operating system: " + operatingSystem);
			System.exit(-1);
		}

		System.setProperty("org.lwjgl.librarypath", nativesPath + "/" + ext);
	}

	public static boolean isCloseRequested() {
		return forceClose;
	}
}
