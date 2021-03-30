package map.architecture;

import java.io.File;
import java.io.FilenameFilter;

import org.joml.Vector3f;

import dev.Console;
import dev.Debug;
import gl.Camera;
import gl.line.LineRender;
import gl.map.architecture.render.ArcRender;
import map.architecture.components.ArcNavNode;
import map.architecture.components.ArcNavigation;
import scene.Scene;
import util.Colors;

public class ArchitectureHandler {
	
	private Architecture architecture;
	
	public static String[] validMaps = new String[0];

	public static void pollValidMaps() {
		File f = new File("maps/");
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				return name.endsWith(".arc");
			}
		};

		// This is how to apply the filter
		String[] pathnames = f.list(filter);
		validMaps = new String[pathnames.length];
		int i = 0;
		for (String pathname : pathnames) {
			validMaps[i] = pathname.replace("maps/", "").replace(".arc", "").toLowerCase();
			i++;
		}
	}
	
	public ArchitectureHandler() {
		ArcRender.init();
	}

	public void load(Scene scene, Vector3f vec, String path) {
		architecture = ArcLoader.load(scene, path, vec, true);
		architecture.callCommand("spawn_player");
	}
	
	public void cleanUp() {
		architecture.cleanUp();
		ArcRender.cleanUp();
	}

	public void render(Camera camera) {
		architecture.pollTriggers();
		architecture.render(camera);
		
		if (Debug.viewNavMesh) {
			ArcNavigation nav = architecture.getNavigation();
			for(ArcNavNode node : nav.getNavFaces()) {
				LineRender.drawLine(node.getPosition(), Vector3f.add(node.getPosition(), new Vector3f(0,5,0)));
				
				for(short s : node.getNeighbors()) {
					ArcNavNode neighbor = nav.getNode(s);
					LineRender.drawLine(node.getPosition(), neighbor.getPosition(), Colors.WHITE);
				}
			}
		}
	}

	public Architecture getArchitecture() {
		return architecture;
	}

	public static boolean isValidMap(String map) {
		String maplc = map.toLowerCase();
		for(String validMap : validMaps) {
			if (validMap.equals(maplc))
				return true;
		}
		
		return false;
	}
}
