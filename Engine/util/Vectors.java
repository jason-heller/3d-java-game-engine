package util;

import org.joml.Vector3f;
import org.joml.Vector3fc;

// Just a utility class
public class Vectors {
	public static final Vector3f ZERO = new Vector3f(0, 0, 0);
	public static final Vector3f ALL = new Vector3f(1, 1, 1);
	
	public static final Vector3f POSITIVE_X = new Vector3f(1, 0, 0);
	public static final Vector3f POSITIVE_Y = new Vector3f(0, 1, 0);
	public static final Vector3f POSITIVE_Z = new Vector3f(0, 0, 1);
	public static final Vector3f NEGATIVE_X = new Vector3f(-1, 0, 0);
	public static final Vector3f NEGATIVE_Y = new Vector3f(0, -1, 0);
	public static final Vector3f NEGATIVE_Z = new Vector3f(0, 0, -1);
	
	/*public static Vector3f orthoCoplanar(Vector3f vector, Vector3f planeNormal) {
		Vector3f vCrossNormal = vector.cross(planeNormal);
		
		return vCrossNormal.orthogonalize();
	}*/
	
	// These are stop-gaps from updating to the new org.joml package, should remove these eventually //
	
	public static Vector3f add(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1).add(v2);
	}
	
	public static Vector3f sub(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1).sub(v2);
	}

	public static Vector3f mul(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1).mul(v2);
	}
	
	public static Vector3f mul(Vector3f v, float f) {
		return new Vector3f(v).mul(f);
	}
	
	public static Vector3f div(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1).div(v2);
	}
	
	public static Vector3f div(Vector3f v, float f) {
		return new Vector3f(v).div(f);
	}

	public static Vector3f cross(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1).cross(v2);
	}
	
	// TODO: Remove
	public static float dot(Vector3f v1, Vector3f v2) {
		return v1.dot(v2);
	}

	public static boolean isZero(Vector3f v) {
		return v.equals(0f, 0f, 0f);
	}

	// TODO: Remove
	public static float distanceSquared(Vector3f v1, Vector3f v2) {
		return v1.distanceSquared(v2);
	}
}
