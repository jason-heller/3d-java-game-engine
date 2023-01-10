package dev;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import core.App;
import geom.Plane;
import gl.Camera;
import gl.line.LineRender;
import io.Input;
import map.architecture.Architecture;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.util.BspRaycast;
import map.architecture.vis.Bsp;
import scene.mapscene.MapScene;
import ui.UI;
import util.Colors;
import util.GeomUtil;
import util.MathUtil;

import static map.RailList.BLOCK_SIZE;

public class RailBuilder {
	
	static Map<Vector3f, Vector3f> railMap = new HashMap<>();
	
	private static final Vector3f OFFSET = new Vector3f(0f,.1f,0f);
	
	public static void update(Camera camera, Architecture arc) {
		BspRaycast ray = arc.raycast(camera.getPosition(), camera.getDirectionVector());
		
		if (ray == null)
			return;
			
		Bsp bsp = arc.bsp;
		ArcFace face = ray.getFace();
		
		int lastEdge = face.firstEdge + face.numEdges;
		
		float bestScore = Float.MAX_VALUE;
		Vector3f[] closestEdge = null;
		
		Vector3f pos = Vector3f.add(camera.getPosition(), Vector3f.mul(camera.getDirectionVector(), ray.getDistance()));
		
		Plane plane = bsp.planes[face.planeId];
		
		if (plane.normal.y == 0f)
			return;
		
		for(int i = face.firstEdge; i < lastEdge; i++) {
			int sign = (int) Math.signum(bsp.surfEdges[i]);
			int edgeId = Math.abs(bsp.surfEdges[i]);
			ArcEdge edge = bsp.edges[edgeId];
			
			Vector3f vertexStart = bsp.vertices[sign > 0 ? edge.start : edge.end];
			Vector3f vertexEnd = bsp.vertices[sign > 0 ? edge.end : edge.start];

			float score = GeomUtil.pointDistanceToEdge(pos, vertexStart, vertexEnd);
			
			if (score < bestScore) { 
				bestScore = score;
				closestEdge = new Vector3f[] {vertexStart, vertexEnd};
			}
		}
		
		if (closestEdge == null)
			return;
		
		LineRender.drawLine(Vector3f.add(closestEdge[0], OFFSET), Vector3f.add(closestEdge[1], OFFSET), railMap.containsKey(closestEdge[0]) ? Colors.RED : Colors.GREEN);
		LineRender.drawBox(pos, new Vector3f(1,1,1), Colors.BLUE);
		
		UI.drawRect(639 , 359, 2, 2, Colors.GREEN);
		
		if (Input.isPressed(Input.KEY_LMB)) {
			railMap.put(closestEdge[0], closestEdge[1]);
		}
		
		if (Input.isPressed(Input.KEY_RMB)) {
			railMap.remove(closestEdge[0], closestEdge[1]);
		}
		
		for(Vector3f key : railMap.keySet()) {
			if (key.equals(closestEdge[0]))
				continue;
			
			LineRender.drawLine(key, railMap.get(key), Colors.YELLOW);
		}
	}
	
	public static void buildRailList() {
		if (!(App.scene instanceof MapScene))
			return;

		MapScene scene = (MapScene)App.scene;
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
			for(Vector3f start : railMap.keySet()) {
				Vector3f end = railMap.get(start);
				
				List<org.joml.Vector2f> points = MathUtil.bresenham((int)start.x/BLOCK_SIZE, (int)start.z/BLOCK_SIZE, (int)end.x/BLOCK_SIZE, (int)end.z/BLOCK_SIZE);
				
				outputStream.writeFloat(start.x);
				outputStream.writeFloat(start.y);
				outputStream.writeFloat(start.z);
				outputStream.writeFloat(end.x);
				outputStream.writeFloat(end.y);
				outputStream.writeFloat(end.z);
				outputStream.writeByte(0);		// This will be the rail type (metal, wooden, stone) and other flags
				
				for(int i = 0; i < points.size(); i++) {
					org.joml.Vector2f blockPos = points.get(i);
					int blockId = 1 + (int)blockPos.x + ((int)blockPos.y * numBlocksX);
					
					if (i == points.size() - 1)
						blockId = -blockId;
					
					outputStream.writeShort(blockId);
				}
			}
			
			outputStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
}
