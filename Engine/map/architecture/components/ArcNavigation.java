package map.architecture.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import org.joml.Vector3f;

import scene.entity.util.NavigableEntity;

public class ArcNavigation {
	
	private ArcNavNode[] broadphaseNodes;
	private ArcNavNode[] narrowphaseNodes;
	
	public void initBroadphase(short numNavElements) {
		this.broadphaseNodes = new ArcNavNode[numNavElements];
	}

	public void initNarrowphase(short numNavElements) {
		narrowphaseNodes = new ArcNavNode[numNavElements];
	}
	
	public void addNode(int index, float x, float y, float z, int leaf, short[] neighbors) {
		broadphaseNodes[index] = new ArcNavNode(x, y, z, leaf, neighbors);
	}
	
	public void addFace(int index, float x, float y, float z, int leaf, short[] neighbors) {
		narrowphaseNodes[index] = new ArcNavNode(x, y, z, leaf, neighbors);
	}
	
	public int getNearest(Vector3f pos) {
		ArcNavNode[] navArray = broadphaseNodes;
		
		if (broadphaseNodes.length == 0) {
			navArray = narrowphaseNodes;
		}
		
		float closestDistance = Vector3f.distanceSquared(pos, navArray[0].getPosition());
		int index = 0;
		
		for(int i = 1; i < navArray.length; i++) {
			float nodeDist = Vector3f.distanceSquared(pos, navArray[i].getPosition());
			if (nodeDist < closestDistance) {
				closestDistance = nodeDist;
				index = i;
			}
		}
		
		return index;
	}
	
	public boolean navigateTo(NavigableEntity entity, Vector3f target) {
		// long time = System.currentTimeMillis();
		ArcNavNode[] navArray = narrowphaseNodes;
		short[] neighbors;
		int targetNode;
		
		if (target == null) {
			// Pick random target
			targetNode = (int)(Math.random() * narrowphaseNodes.length);
			target = narrowphaseNodes[targetNode].getPosition();
			
		} else {
			targetNode = getNearest(target);
		}
		
		NodeDetails[] nodeDetails = new NodeDetails[navArray.length];
		for(int i = 0; i < navArray.length; i++) {
			nodeDetails[i] = new NodeDetails();
		}
		
		List<Integer> closedList = new ArrayList<>();
		TreeMap<Float, Integer> openList = new TreeMap<>();
		Stack<Integer> path = entity.getPath();
		path.clear();
		
		int currentNode = getNearest(entity.pos);
		openList.put(0f, currentNode);
		nodeDetails[currentNode].score = 0f;
		
		boolean pathComplete = false;
		
		while(!openList.isEmpty() && !pathComplete) {
			int id = openList.firstEntry().getValue();
			ArcNavNode node = navArray[id];
			neighbors = node.getNeighbors();
			openList.remove(openList.firstKey());
			closedList.add(id);
			
			for(int i = 0; i < neighbors.length; i++) {
				int successorId = neighbors[i];
				ArcNavNode successor = navArray[successorId];
				
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
					}
				}
			}
		}
		
		if (!pathComplete) {
			return false;
		}
		
		int currentId = targetNode;
		while(currentId != currentNode) {
			path.add(currentId);
			currentId = nodeDetails[currentId].parent;
		}
		
		/*
		if (path.size() > 1) {
			float d1 = Vector3f.distanceSquared(entity.pos, navArray[currentId].getPosition());
			float d2 = Vector3f.distanceSquared(entity.pos, navArray[path.get(path.size()-2)].getPosition());
			
			if (d2 < d1) {
				entity.setCurrentNode(path.get(path.size()-2));
				path.remove(path.size()-1);
			}
		}*/
		
		// long diff = System.currentTimeMillis() - time;
		// Console.log("took " + diff + " ms to calculate");
		return true;
	}

	public ArcNavNode[] getNavFaces() {
		return narrowphaseNodes;
	}

	public ArcNavNode getNode(int index) {
		if (index >= 0) {
			return narrowphaseNodes[index];
		}
		
		return broadphaseNodes[-index];
	}

	public void biasedWalk(Vector3f navTarget) {
		// TODO Auto-generated method stub
		
		
	}
}

class NodeDetails {
	public float score = Float.MAX_VALUE;
	public float weight, heuristic;
	public int parent = -1;
}
