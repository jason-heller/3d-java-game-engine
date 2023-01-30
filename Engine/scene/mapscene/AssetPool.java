 package scene.mapscene;

import java.util.LinkedList;
import java.util.Queue;

import core.Resources;
import io.DOOMLoader;

public class AssetPool {
	private static Queue<String> loadedTextures = new LinkedList<>();
	private static Queue<String> loadedSounds = new LinkedList<>();
	private static Queue<String> loadedModels = new LinkedList<>();

	public static void loadInGameAssets() {
		//addTexture("dmg_screen_effect", "gui/jelly.png");
		//addTexture("dmg_slash", "gui/slash.png");
		
		Resources.addModel("untitled", "models/SKATER.MF");
		DOOMLoader.load("src/res/models/SKATER.DOOM");
		
		addSound("ollie", "temp_ollie.ogg");
		addSound("grind", "temp_grind_rock.ogg");
		addSound("ride_rock", "temp_ride_rock.ogg");
		addSound("ride_grass", "temp_ride_grass.ogg");
	}

	private static void addModel(String key, String val) {
		Resources.addModel(key, val);
		
		loadedModels.add(key);
	}
	
	private static void addSound(String key, String val) {
		Resources.addSound(key, val);
		loadedSounds.add(key);
	}
	
	private static void addSound(String key, String val, boolean hasVariance) {
		Resources.addSound(key, val, hasVariance);
		loadedSounds.add(key);
	}

	private static void addSound(String key, String val, int iterations, boolean hasVariance) {
		Resources.addSound(key, val, iterations, hasVariance);
		loadedSounds.add(key);
	}

	private static void addTexture(String key, String val) {
		Resources.addTexture(key, val);
		loadedTextures.add(key);
	}

	public static void unload() {
		for (String asset : loadedTextures)
			Resources.removeTexture(asset);

		for (String asset : loadedSounds)
			Resources.removeSound(asset);

		for (String asset : loadedModels)
			Resources.removeModel(asset);

		loadedModels.clear();
		loadedSounds.clear();
		loadedTextures.clear();
	}
}
