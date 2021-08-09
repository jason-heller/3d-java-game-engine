package map.architecture.vis;

import java.util.List;

import org.joml.Vector3f;

public class BspNode {
	public int planeNum;
	public int[] childrenId = new int[2];
	public Vector3f min, max;
	public short firstFace;
	public short numFaces;
	
	public boolean intersects(Vector3f max2, Vector3f min2) {
		if (min.x > max2.x || max.x < min2.x) return false;
		if (min.y > max2.y || max.y < min2.y) return false;
		if (min.z > max2.z || max.z < min2.z) return false;
	    
	    return true;
	}
}
