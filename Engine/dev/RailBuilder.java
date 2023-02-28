package dev;

import static map.RailList.BLOCK_SIZE;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import core.App;
import dev.cmd.Console;
import geom.Plane;
import gl.Camera;
import gl.line.LineRender;
import io.Input;
import map.Rail;
import map.RailList;
import map.architecture.Architecture;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.util.BspRaycast;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.mapscene.MapScene;
import ui.UI;
import util.Colors;
import util.GeomUtil;
import util.Vectors;

public class RailBuilder {

	static Map<Integer, Vector3f[]> railMap = new HashMap<>();
	static Map<Integer, Integer> railTypeMap = new HashMap<>();
	
	static List<Integer> selected = new ArrayList<>();

	private static final Vector3f OFFSET = new Vector3f(0f, .1f, 0f);

	private static int type = 0;
	private static int highlighted = Integer.MAX_VALUE;

	public static void update(Camera camera, Architecture arc) {
		if (Input.isPressed(Keyboard.KEY_1)) {
			type = 0;
		}

		if (Input.isPressed(Keyboard.KEY_2)) {
			type = 1;
		}

		if (Input.isPressed(Keyboard.KEY_3)) {
			type = 2;
		}
		
		if (Input.isPressed(Keyboard.KEY_TAB)) {
			if (Input.isMouseGrabbed()) 
				Input.requestMouseRelease();
			else
				Input.requestMouseGrab();
		}
		
		if (Input.isDown(Keyboard.KEY_LCONTROL)) {
			if (Input.isPressed(Keyboard.KEY_E))
				RailBuilder.buildRailList();
			if (Input.isPressed(Keyboard.KEY_V))
				RailBuilder.removeFloatingRails();
		}
		
		if (Input.isPressed(Keyboard.KEY_DELETE)) {
			for(int key : selected) {
				railMap.remove(key);
				railTypeMap.remove(key);
			}
			
			selected.clear();
		}

		String typeName;

		switch (type) {
		case 1:
			typeName = "metal";
			break;
		case 2:
			typeName = "#ywood";
			break;
		default:
			typeName = "#bstone";
		}

		UI.drawString("LMB: add rail\n"
				+ "RMB: delete rail\n"
				+ "1-3 toggle type\n"
				+ "tab: toggle mouse\n"
				+ "ctrl + E export\n\n"
				+ "Rail type: " + typeName + "\n"
				+ "#wTotal rails: " + railMap.size(), 1100, 10, .17f, false);

		for (int key : railMap.keySet()) {
			if (key == highlighted)
				continue;

			Vector3f color = null;

			if (selected.contains(key)) {
				color = Colors.alertColor();
			} else {
				switch (railTypeMap.get(key)) {
				case 1:
					color = Colors.WHITE;
					break;
				case 2:
					color = Colors.YELLOW;
					break;
				default:
					color = Colors.BLUE;
				}
			}
			
			
			Vector3f[] line = railMap.get(key);
			LineRender.drawLine(line[0], line[1], color);
		}
		
		BspRaycast ray = arc.raycast(camera.getPosition(), camera.getDirectionVector());

		if (ray == null)
			return;

		Bsp bsp = arc.bsp;
		ArcFace face = ray.getFace();

		int lastEdge = face.firstEdge + face.numEdges;

		float bestScore = Float.MAX_VALUE;
		Vector3f[] closestEdge = null;

		Vector3f pos = Vectors.add(camera.getPosition(), Vectors.mul(camera.getDirectionVector(), ray.getDistance()));

		Plane plane = bsp.planes[face.planeId];

		if (plane.normal.y == 0f)
			return;

		for (int i = face.firstEdge; i < lastEdge; i++) {
			int sign = (int) Math.signum(bsp.surfEdges[i]);
			int edgeId = Math.abs(bsp.surfEdges[i]);
			ArcEdge edge = bsp.edges[edgeId];

			Vector3f vertexStart = bsp.vertices[sign > 0 ? edge.start : edge.end];
			Vector3f vertexEnd = bsp.vertices[sign > 0 ? edge.end : edge.start];

			float score = GeomUtil.pointDistanceToEdge(pos, vertexStart, vertexEnd);

			if (score < bestScore) {
				bestScore = score;
				closestEdge = new Vector3f[] { vertexStart, vertexEnd };
			}
		}

		if (closestEdge == null)
			return;

		highlighted = closestEdge[0].hashCode();
		LineRender.drawLine(Vectors.add(closestEdge[0], OFFSET), Vectors.add(closestEdge[1], OFFSET),
				railMap.containsKey(highlighted) ? Colors.RED : Colors.GREEN);
		LineRender.drawBox(pos, new Vector3f(1, 1, 1), Colors.BLUE);

		UI.drawRect(639, 359, 2, 2, Colors.GREEN);

		if (Input.isPressed(Input.KEY_LMB)) {
			railMap.put(highlighted, closestEdge);
			railTypeMap.put(highlighted, type);
		}

		if (Input.isPressed(Input.KEY_RMB)) {
			railMap.remove(highlighted);
			railTypeMap.remove(highlighted);
		}
	}

	private static void removeFloatingRails() {
		MapScene scene = (MapScene) App.scene;
		Architecture arc = scene.getArchitecture();
		
		selected.clear();
		
		for (int key : railMap.keySet()) {
			Vector3f[] line = railMap.get(key);
			Vector3f start = line[0], end = line[1];
			
			List<BspLeaf> leaves = new ArrayList<>();
			leaves.addAll(arc.bsp.walk(Vectors.sub(start, Vectors.ALL), Vectors.add(start, Vectors.ALL)));
			for(BspLeaf l : arc.bsp.walk(Vectors.sub(end, Vectors.ALL), Vectors.add(end, Vectors.ALL))) {
				if (!leaves.contains(l))
					leaves.add(l);
			}
			
			boolean delete = true;
			
			for(BspLeaf leaf : leaves) {
				
				for(int i = 0; i < leaf.numFaces; i++) {
					ArcFace face = arc.bsp.faces[arc.bsp.leafFaceIndices[leaf.firstFace + i]];
					
					for(int j = 0; j < face.numEdges; j++) {
						ArcEdge edge = arc.bsp.edges[Math.abs(arc.bsp.surfEdges[face.firstEdge + j])];
						
						Vector3f e1 = arc.bsp.vertices[edge.start];
						Vector3f e2 = arc.bsp.vertices[edge.end];
						
						if (start.distanceSquared(e1) < 1 || start.distanceSquared(e2) < 1) {
							delete = false;
							break;
						}
						
						if (end.distanceSquared(e1) < 1 || end.distanceSquared(e2) < 1) {
							delete = false;
							break;
						}
					}
					
					if (!delete)
						break;
				}
			}
			
			if (delete) {
				selected.add(key);
			}
		}
	}

	public static void buildRailList() {
		if (!(App.scene instanceof MapScene))
			return;

		MapScene scene = (MapScene) App.scene;
		Architecture arc = scene.getArchitecture();

		File outputFile = new File("maps/" + arc.getMapName() + ".rail");

		int numBlocksX = (int) Math.ceil((arc.bsp.max.x - arc.bsp.min.x) / BLOCK_SIZE);
		int numBlocksZ = (int) Math.ceil((arc.bsp.max.z - arc.bsp.min.z) / BLOCK_SIZE);

		try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile))) {
			outputStream.writeChars("RAIL");
			outputStream.writeByte(1);

			outputStream.writeShort(railMap.keySet().size());
			outputStream.writeShort(numBlocksX);
			outputStream.writeShort(numBlocksZ);

			// Map rails
			for (int key : railMap.keySet()) {
				Vector3f[] line = railMap.get(key);
				int type = railTypeMap.get(key);
				Vector3f start = line[0], end = line[1];

				List<Integer> boxes = new ArrayList<>();

				for (int i = 0; i < numBlocksX; i++) {
					for (int j = 0; j < numBlocksZ; j++) {
						float cx = (arc.bsp.min.x + (i * BLOCK_SIZE));
						float cz = (arc.bsp.min.z + (j * BLOCK_SIZE));

						Line2D railLine = new Line2D.Float(start.x, start.z, end.x, end.z);
						Rectangle2D box = new Rectangle2D.Float(cx, cz, BLOCK_SIZE, BLOCK_SIZE);

						if (railLine.intersects(box)) {
							boxes.add(1 + i + (j * numBlocksX));
						}
					}
				}

				outputStream.writeFloat(start.x);
				outputStream.writeFloat(start.y);
				outputStream.writeFloat(start.z);
				outputStream.writeFloat(end.x);
				outputStream.writeFloat(end.y);
				outputStream.writeFloat(end.z);
				outputStream.writeByte(type); // This will be the rail type (metal, wooden, stone) and other flags

				for (int i = 0; i < boxes.size(); i++) {
					int blockId = boxes.get(i);

					if (i == boxes.size() - 1)
						blockId = -blockId;

					outputStream.writeShort(blockId);
				}
			}

			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Console.highlight("Rail list built successfully!");
	}

	public static void fillList() {
		railMap.clear();
		RailList[] list = ((PlayableScene) App.scene).getArchitecture().getRailList();

		for (int i = 0; i < list.length; i++) {
			RailList rl = list[i];

			if (rl != null) {
				List<Rail> rails = rl.getRails();

				for (Rail rail : rails) {
					int id = rail.getStart().hashCode();
					railMap.put(id, new Vector3f[] { rail.getStart(), rail.getEnd() });
					railTypeMap.put(id, rail.type);
				}
			}
		}
	}
}
