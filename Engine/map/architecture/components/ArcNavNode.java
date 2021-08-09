package map.architecture.components;

import org.joml.Vector3f;

public class ArcNavNode {
	private int[] faceIds;
	private short[] neighbors;
	private int[] edges;
	private Vector3f position;		// TODO: Make this obsolete

	public ArcNavNode(Vector3f position, int[] faceIds, short[] neighbors, int[] edges) {
		this.position = position;
		this.faceIds = faceIds;
		this.neighbors = neighbors;
		this.edges = edges;
	}
	
	public short[] getNeighbors() {
		return neighbors;
	}
	
	public int[] getEdges() {
		return edges;
	}
	
	public int[] getFaceIds() {
		return faceIds;
	}

	public Vector3f getPosition() {
		return position;
	}
}