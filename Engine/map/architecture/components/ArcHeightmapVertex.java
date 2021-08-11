package map.architecture.components;

public class ArcHeightmapVertex {
	public final float offset;
	private float blend;
	
	public ArcHeightmapVertex(float offset, float blend) {
		this.offset = offset;
		this.blend = blend;
	}
	
	public float getBlend() {
		return blend;
	}
}
