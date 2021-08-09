package map.architecture.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import org.joml.Vector3f;

import core.Application;
import dev.Console;
import map.architecture.ArcUtil;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import scene.entity.util.NavigableEntity;

public class ArcNavigation {
	
	private ArcNavNode[] navMesh;
	
	public void initNarrowphase(short numNavElements) {
		navMesh = new ArcNavNode[numNavElements];
	}
	
	public void addNode(int index, Vector3f position, int[] faces, short[] neighbors, int[] edges) {
		navMesh[index] = new ArcNavNode(position, faces, neighbors, edges);
	}
	
	public ArcNavNode getNodeAt(Vector3f pos, Bsp bsp) {
		int id = nodeIdAt(pos, bsp);
		return id == -1 ? null : navMesh[id];
	}
	
	private int nodeIdAt(Vector3f pos, Bsp bsp) {
		
		for(int i = 0; i < navMesh.length; i++) {
			final int[] faceIds = navMesh[i].getFaceIds();
			
			for(int faceId : faceIds) {
				boolean outside = faceContainsPointProjXZ(bsp, bsp.faces[faceId], pos);
				
				if (!outside) {
					return i;
				}
			}
		}
		
		return -1;
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

	/**
	 * Similar to ArcUtil.faceContainsPoint(); This is a special case where the face
	 * is always projected onto the XZ plane. Since this will be called often, this
	 * is used over the general case method for optimization
	 * 
	 * @param bsp
	 *            The current BSP
	 * @param face
	 *            The face being tested
	 * @param point
	 *            The point we are testing against
	 * @return true if the point falls within the face when projected onto the XZ
	 *         plane, false otherwise
	 */
	private boolean faceContainsPointProjXZ(Bsp bsp, ArcFace face, Vector3f point) {
		final int edgeStart = face.firstEdge;
		final int edgeEnd = edgeStart + face.numEdges;
		
		boolean outside = false;
		
		for(int j = edgeStart; j < edgeEnd; j++) {
			Vector3f p1, p2;
			int edgeId = bsp.surfEdges[j];
			
			if (edgeId < 0) {
				p1 = bsp.vertices[bsp.edges[-edgeId].end];
				p2 = bsp.vertices[bsp.edges[-edgeId].start];
			} else {
				p1 = bsp.vertices[bsp.edges[edgeId].start];
				p2 = bsp.vertices[bsp.edges[edgeId].end];
			}

			if (((p2.x - p1.x) * (point.z - p1.z) - (p2.z - p1.z) * (point.x - p1.x)) > 0) {
				outside = true;
				break;
			}
		}
		
		return outside;
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
