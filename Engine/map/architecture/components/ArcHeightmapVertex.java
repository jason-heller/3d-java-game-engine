package map.architecture.components;

import org.joml.Vector3f;

public class ArcHeightmapVertex {
	public final Vector3f vertex;
	private float blend;
	
	public ArcHeightmapVertex(float x, float y, float z, float blend) {
		vertex = new Vector3f(x, y, z);
		this.blend = blend;
	}
	
	public float getBlend() {
		return blend;
	}
}
