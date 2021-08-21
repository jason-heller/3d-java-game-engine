package map.architecture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joml.Vector3f;
import org.joml.Vector4f;

import dev.Debug;
import dev.cmd.Console;
import geom.Plane;
import gl.Camera;
import gl.arc.ArcRenderMaster;
import gl.line.LineRender;
import map.architecture.components.ArcNavNode;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcRoom;
import map.architecture.components.GhostPoi;
import map.architecture.read.ArcLoader;
import map.architecture.vis.Bsp;
import scene.Scene;
import util.Colors;
import util.MathUtil;

public class ArchitectureHandler {
	
	private Architecture architecture;
	
	private static boolean changeMap = false;
	public static String[] validMaps = new String[0];
	private static String[] mapSequence = readMapSequenceFile();
	private static int currentMap = 0;

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

	private static String[] readMapSequenceFile() {
		List<String> lines = new ArrayList<String>();
		Scanner sc;
		
		try {
			sc = new Scanner(new File("src/res/maps/map_sequence.txt"));
			while (sc.hasNextLine()) {
				lines.add(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return lines.toArray(new String[0]);
	}

	public ArchitectureHandler() {
		ArcRenderMaster.init();
	}

	public void load(Scene scene, Vector3f vec, String path) {
		architecture = ArcLoader.load(scene, path, vec, true);
	}
	
	public void cleanUp() {
		architecture.cleanUp();
		ArcRenderMaster.cleanUp();
	}
	
	public void debugRender(Camera camera) {
		Bsp bsp = architecture.bsp;
		ArcNavigation navigation = architecture.getNavigation();
		
		if (Debug.viewNavNode) {
			ArcNavNode node = navigation.getNodeAt(camera.getPosition(), bsp);
			if (node != null) {
				Plane plane = bsp.planes[node.getPlaneId()];
				Vector3f p = node.getPosition();
				float dx = node.getWidth();
				float dz = node.getLength();
				
				Vector3f tl = plane.raycastPoint(new Vector3f(p.x-dx, -99999, p.z+dz), Vector3f.Y_AXIS);
				Vector3f tr = plane.raycastPoint(new Vector3f(p.x+dx, -99999, p.z+dz), Vector3f.Y_AXIS);
				Vector3f bl = plane.raycastPoint(new Vector3f(p.x-dx, -99999, p.z-dz), Vector3f.Y_AXIS);
				Vector3f br = plane.raycastPoint(new Vector3f(p.x+dx, -99999, p.z-dz), Vector3f.Y_AXIS);
				
				tl.add(plane.normal);
				tr.add(plane.normal);
				bl.add(plane.normal);
				br.add(plane.normal);
				
				LineRender.drawLine(tl, tr, Colors.alertColor());
				LineRender.drawLine(tr, br, Colors.alertColor());
				LineRender.drawLine(br, bl, Colors.alertColor());
				LineRender.drawLine(bl, tl, Colors.alertColor());
				
				for(int i = 0; i < node.getNeighbors().length; i++) {
					short nid = node.getNeighbors()[i];
					LineRender.drawLine(node.getPosition(), navigation.getNode(nid).getPosition(), Colors.WHITE);
					LineRender.drawPoint(node.getPosition());
				}
			}
		}
		
		if (Debug.viewNavPois) {
			for(int i = 1; i < bsp.rooms.length; i++) {
				ArcRoom room = bsp.rooms[i];
				for(GhostPoi poi : room.getGhostPois()) {
					Vector3f pos = Vector3f.add(poi.getPosition(), Vector3f.Y_AXIS);
					LineRender.drawPoint(poi.getPosition());
					LineRender.drawLine(pos, Vector3f.add(pos, MathUtil.eulerToVectorDeg(poi.getRotation().x, poi.getRotation().y)));
				}
			}
		}
		
		if (Debug.viewNavMesh) {
			ArcNavigation nav = architecture.getNavigation();
			for(ArcNavNode node : nav.getNavFaces()) {
				
				Plane plane = bsp.planes[node.getPlaneId()];
				Vector3f p = node.getPosition();
				float dx = node.getWidth();
				float dz = node.getLength();
				
				Vector3f tl = plane.raycastPoint(new Vector3f(p.x-dx, -99999, p.z+dz), Vector3f.Y_AXIS);
				Vector3f tr = plane.raycastPoint(new Vector3f(p.x+dx, -99999, p.z+dz), Vector3f.Y_AXIS);
				Vector3f bl = plane.raycastPoint(new Vector3f(p.x-dx, -99999, p.z-dz), Vector3f.Y_AXIS);
				Vector3f br = plane.raycastPoint(new Vector3f(p.x+dx, -99999, p.z-dz), Vector3f.Y_AXIS);
				
				tl.add(plane.normal);
				tr.add(plane.normal);
				bl.add(plane.normal);
				br.add(plane.normal);
				
				LineRender.drawLine(tl, tr, Colors.YELLOW);
				LineRender.drawLine(tr, br, Colors.YELLOW);
				LineRender.drawLine(br, bl, Colors.YELLOW);
				LineRender.drawLine(bl, tl, Colors.YELLOW);
			}
		}
	}

	public void render(Camera camera, Vector4f clipPlane) {
		boolean hasLighting = architecture.getLightmap().isActive();
		architecture.pollTriggers();
		architecture.render(camera, clipPlane, hasLighting);
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

	public void update(Camera camera) {
		if (changeMap) {
			Console.send("map " + mapSequence[currentMap]);
			changeMap = false;
			return;
		}
		
		architecture.determineVisibleLeafs(camera);
	}
	
	public boolean isSkyboxEnabled() {
		return architecture.hasSkybox;
	}
	
	public static void nextMap() {
		currentMap++;
		changeMap = true;
		
	}
}
