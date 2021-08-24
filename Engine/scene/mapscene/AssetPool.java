package scene.mapscene;

import java.util.LinkedList;
import java.util.Queue;

import core.Resources;
import dev.cmd.Console;

public class AssetPool {
	private static Queue<String> loadedTextures = new LinkedList<>();
	private static Queue<String> loadedSounds = new LinkedList<>();
	private static Queue<String> loadedModels = new LinkedList<>();

	public static void loadInGameAssets() {
		addTexture("dmg_screen_effect", "gui/jelly.png");
		addTexture("dmg_slash", "gui/slash.png");

		addSound("walk_rock", "walk_rock.ogg", 1, true);
		addSound("walk_dirt", "walk_dirt.ogg", 2, true);
		addSound("walk_mud", "walk_mud.ogg", 3, true);
		addSound("walk_grass", "walk_grass.ogg", 3, true);
		addSound("ghost_voice", "ghost/ghost_voice.ogg", 3, true);
		addSound("fall", "player/fall.ogg");
		
		addSound("cicadas", "ambient/cicadas.ogg");
		addSound("white_noise", "ambient/white_noise.ogg");
		
		addSound("spiritbox", "tool/spiritbox.ogg");
		addSound("camera_snap", "tool/camera_snap.ogg");
		addSound("place", "tool/place_wall.ogg");
		
		addModel("spiritbox", "models/spiritbox.mod");
		addModel("camera", "models/camera.mod");
		addModel("cam_tripod", "models/cam_tripod.mod");
		addModel("tablet", "models/tablet.mod");
		addModel("emf", "models/emf.mod");
		addTexture("emf", "models/emf.png");
		
		Resources.addObjModel("trash", "entity/prop/trash.obj");
		addTexture("trash", "entity/prop/trash.png");
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
