package map.architecture.util;

import map.architecture.components.ArcFace;

public class BspRaycast {
	private ArcFace face;
	private float dist;
	
	public BspRaycast(ArcFace face, float dist) {
		this.face = face;
		this.dist = dist;
	}
	
	public ArcFace getFace() {
		return face;
	}
	
	public float getDistance() {
		return dist;
	}
}
