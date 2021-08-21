package map.architecture.components;

import org.joml.Vector3f;

public class ArcNavNode {
	private int planeId;
	private short[] neighbors;
	
	private float width, length;
	
	private Vector3f position;

	public ArcNavNode(Vector3f position, int planeId, short[] neighbors, float width, float length) {
		this.position = position;
		this.planeId = planeId;
		this.neighbors = neighbors;
		this.width = width;
		this.length = length;
	}
	
	public short[] getNeighbors() {
		return neighbors;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getLength() {
		return length;
	}
	
	public int getPlaneId() {
		return planeId;
	}

	public Vector3f getPosition() {
		return position;
	}
}