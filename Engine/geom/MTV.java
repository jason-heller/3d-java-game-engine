package geom;

import org.joml.Vector3f;

import dev.Console;

/** Minimum Translation Vector
 * @author Jason
 *
 */
public class MTV implements Comparable<MTV> {
	
	private Vector3f axis;
	private float depth;
	private Plane plane;
	
	public MTV() {
		depth = Float.MAX_VALUE;
		axis = new Vector3f();
	}
	
	public MTV(float depth, Vector3f axis) {
		this.depth = depth;
		this.axis = axis;
	}

	public boolean testAxis(float max1, float min1, float max2, float min2, Vector3f axis) {
		float axisLengthSquared = Vector3f.dot(axis, axis);
		if (axisLengthSquared < 1.0e-8f)
	        return true;
		
		// min1,max1 are the object to be moved (the focus)
		// min2,max2 are the object to be collided with
		float o1 = max2 - min1;		// RHS
		float o2 = max1 - min2;		// LHS
		
		if (o1 <= 0f || o2 <= 0f)
			return false;
		
		float overlap = o1 < o2 ? o1 : -o2;

		Vector3f sep = Vector3f.mul(axis, overlap / axisLengthSquared);
		float sepLengthSquared = Vector3f.dot(sep, sep);
		
		if (sepLengthSquared < depth) {
			depth = sepLengthSquared;
			this.axis.set(sep);
		}
		
		return true;
	}

	public void finalize(Vector3f pt) {
		axis.normalize();
		depth = (float)Math.sqrt(depth) * 1.01f;
		this.plane = new Plane(pt, axis);
	}
	
	public Vector3f getMTV() {
		return Vector3f.mul(axis, depth);
	}
	
	public float getDepth() {
		return depth;
	}
	
	public Vector3f getAxis() {
		return axis;
	}

	@Override
	public int compareTo(MTV o) {
		return (int) (depth - o.getDepth());
	}
	
	public Plane getPlane() {
		return plane;
	}
}
