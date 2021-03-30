package map.architecture.components;

import org.joml.Vector3f;

public class ArcNavNode {
	private Vector3f position;
	private int leafId;
	private short[] neighbors;

	public ArcNavNode(float x, float y, float z, int leaf, short[] neighbors) {
		position = new Vector3f(x, y, z);
		this.leafId = leaf;
		this.neighbors = neighbors;
	}
	
	public short[] getNeighbors() {
		return neighbors;
	}

	public Vector3f getPosition() {
		return position;
	}
	
	public int getLeafId() {
		return leafId;
	}
}