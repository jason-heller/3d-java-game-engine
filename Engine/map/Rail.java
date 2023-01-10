package map;

import org.joml.Vector3f;

public class Rail {
	public Vector3f start, end;
	private String type;
	
	public Rail(Vector3f start, Vector3f end, String type) {
		this.start = start;
		this.end = end;
		this.type = type;
	}
}
