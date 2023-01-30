package map;

import org.joml.Vector3f;

public class Rail {
	private final Vector3f start, end;
	public final int type;
	
	public Rail(Vector3f start, Vector3f end, int type) {
		this.start = start;
		this.end = end;
		this.type = type;
	}
	
	public Vector3f getStart() {
		return start;
	}
	
	public Vector3f getEnd() {
		return end;
	}
}
