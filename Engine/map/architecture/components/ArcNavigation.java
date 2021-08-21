package map.architecture.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import org.joml.Vector3f;

import core.Application;
import geom.Plane;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import scene.entity.util.NavigableEntity;

public class ArcNavigation {
	
	private ArcNavNode[] navMesh;
	
	public void initNarrowphase(short numNavElements) {
		navMesh = new ArcNavNode[numNavElements];
	}
	
	public void addNode(int index, Vector3f position, int planeId, short[] neighbors, float width, float length) {
		navMesh[index] = new ArcNavNode(position, planeId, neighbors, width, length);
	}
	
	public ArcNavNode getNodeAt(Vector3f pos, Bsp bsp) {
		int id = nodeIdAt(pos, bsp);
		return id == -1 ? null : navMesh[id];
	}
	
	private int nodeIdAt(Vector3f pos, Bsp bsp) {
		float nearestY = Float.POSITIVE_INFINITY;
		int id = -1;
		
		for(int i = 0; i < navMesh.length; i++) {
			ArcNavNode node = navMesh[i];
			final int planeId = node.getPlaneId();
			
			Plane plane = bsp.planes[planeId];
			float signDistPlane = plane.raycast(pos, new Vector3f(0, -1, 0));
			
			if (signDistPlane == Float.POSITIVE_INFINITY)
				continue;
			
			if (Math.abs(pos.x - node.getPosition().x) > node.getWidth())
				continue;
			
			if (Math.abs(pos.z - node.getPosition().z) > node.getLength())
				continue;
			
			//float dist = Vector3f.distanceSquared(pos, node.getPosition());
			if (signDistPlane < nearestY) {
				nearestY = signDistPlane;
				id = i;
			}
		}
		
		return id;
	}
	
	public boolean navigateTo(NavigableEntity entity, Vector3f target) {
		
		Bsp bsp = ((PlayableScene)Application.scene).getArchitecture().bsp;

		short[] neighbors;
		int targetNode;
		
		if (target == null) {
			// Pick random target
			targetNode = (int)(Math.random() * navMesh.length);
			target = navMesh[targetNode].getPosition();
			
		} else {
			targetNode = nodeIdAt(target, bsp);
		}
		
		NodeDetails[] nodeDetails = new NodeDetails[navMesh.length];
		for(int i = 0; i < navMesh.length; i++) {
			nodeDetails[i] = new NodeDetails();
		}
		
		List<Integer> closedList = new ArrayList<>();
		TreeMap<Float, Integer> openList = new TreeMap<>();
		Stack<Integer> path = entity.getPath();
		path.clear();
		
		int currentNodeId = nodeIdAt(entity.pos, bsp);	// Get our starting Node
		if (currentNodeId == -1)
			return false;
		
		openList.put(0f, currentNodeId);
		nodeDetails[currentNodeId].score = 0f;
		
		entity.currentNodeId = currentNodeId;
		
		boolean pathComplete = false;
		
		while(!openList.isEmpty() && !pathComplete) {
			int id = openList.firstEntry().getValue();
			ArcNavNode node = navMesh[id];
			neighbors = node.getNeighbors();
			openList.remove(openList.firstKey());
			closedList.add(id);
			
			for(int i = 0; i < neighbors.length; i++) {
				int successorId = neighbors[i];
				ArcNavNode successor = navMesh[successorId];
				
				if (successorId == targetNode) {
					pathComplete = true;
					NodeDetails details = nodeDetails[successorId];
					details.parent = id;
					break;
				}
				
				if (!closedList.contains(successorId)) {
					NodeDetails details = nodeDetails[successorId];
					float newWeight = details.weight + 1.0f;
					
					// Calculate heuristic and distance
					Vector3f thisNodesPos = successor.getPosition();
					float hueristic = Vector3f.distanceSquared(thisNodesPos, target);
					
					float score = hueristic + newWeight;
					
					if (details.score == Float.MAX_VALUE || score < details.score) {
						openList.put(score, successorId);
						details.score = score;
						details.weight = newWeight;
						details.heuristic = hueristic;
						details.parent = id;
						details.relativeId = neighbors[i];
					}
				}
			}
		}
		
		if (!pathComplete) {
			return false;
		}
		
		int currentId = targetNode;
		while(currentId != currentNodeId) {
			path.add(currentId);
			currentId = nodeDetails[currentId].parent;
		}

		return true;
	}

	public ArcNavNode[] getNavFaces() {
		return navMesh;
	}

	public ArcNavNode getNode(int index) {
		return index < 0 ? null : navMesh[index];
	}
}

class NodeDetails {
	public short relativeId;
	public float score = Float.MAX_VALUE;
	public float weight, heuristic;
	public int parent = -1;
}
