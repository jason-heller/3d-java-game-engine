package geom;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BoundingBox {

	public Vector3f bounds;
	public Vector3f center;
	
	protected float scale;
	public Vector3f X, Y, Z;
	
	public float yaw, pitch, roll;
	public float overlap;

	public BoundingBox(float x, float y, float z, float w, float h, float l) {
		this.center = new Vector3f(x, y, z);
		this.X = new Vector3f(Vector3f.X_AXIS);
		this.Y = new Vector3f(Vector3f.Y_AXIS);
		this.Z = new Vector3f(Vector3f.Z_AXIS);
		this.bounds = new Vector3f(w, h, l);
		yaw = pitch = roll = 0f;
	}

	public BoundingBox(Vector3f center, float w, float h, float l) {
		this.center = center;
		this.X = new Vector3f(Vector3f.X_AXIS);
		this.Y = new Vector3f(Vector3f.Y_AXIS);
		this.Z = new Vector3f(Vector3f.Z_AXIS);
		this.bounds = new Vector3f(w, h, l);
		yaw = pitch = roll = 0f;
	}

	public BoundingBox(Vector3f center, Vector3f max, Vector3f min) {
		this(center, Vector3f.sub(max, min));

	}
	
	public BoundingBox(Vector3f center, Vector3f bounds) {
		this.bounds = bounds;
		this.center = center;
		this.X = new Vector3f(Vector3f.X_AXIS);
		this.Y = new Vector3f(Vector3f.Y_AXIS);
		this.Z = new Vector3f(Vector3f.Z_AXIS);
		yaw = pitch = roll = 0f;

	}
	
	public void update(Vector3f pos, Vector3f rot, float scale) {
		this.center = pos;
		this.X.set(1f,0f,0f);
		this.Y.set(0f,1f,0f);
		this.Z.set(0f,0f,1f);
		Matrix3f mat = new Matrix3f();
		mat.rotateX(rot.x);
		mat.rotateY(rot.y);
		mat.rotateZ(rot.z);
		mat.transform(X);
		mat.transform(Y);
		mat.transform(Z);

		this.scale = scale;
	}
	
	public Vector3f intersects(BoundingBox box) {
		Vector4f axis = new Vector4f(0, 0, 0, 100);

		// Ugly but works
		if ((axis = axisTest(axis, X, box)) == null)
			return null;
		if ((axis = axisTest(axis, Y, box)) == null)
			return null;
		if ((axis = axisTest(axis, Z, box)) == null)
			return null;
		if ((axis = axisTest(axis, box.X, box)) == null)
			return null;
		if ((axis = axisTest(axis, box.Y, box)) == null)
			return null;
		if ((axis = axisTest(axis, box.Z, box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(X, box.X), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(X, box.Y), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(X, box.Z), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(Y, box.X), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(Y, box.Y), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(Y, box.Z), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(Z, box.X), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(Z, box.Y), box)) == null)
			return null;
		if ((axis = axisTest(axis, Vector3f.cross(Z, box.Z), box)) == null)
			return null;

		return new Vector3f(axis.x, axis.y, axis.z);
	}

	private Vector4f axisTest(Vector4f overlapAxis, Vector3f axis, BoundingBox other) {
		if (axis.isZero())
			return overlapAxis;
		float dot, rad;

		// Calculate extremes for this OBB
		dot = Vector3f.dot(axis, center);
		rad = Math.abs(Vector3f.dot(axis, X)) * bounds.x*scale + Math.abs(Vector3f.dot(axis, Y)) * bounds.y*scale
				+ Math.abs(Vector3f.dot(axis, Z)) * bounds.z*scale;

		float selfMin = dot - rad;
		float selfMax = dot + rad;

		// Calculate extremes for other OBB
		dot = Vector3f.dot(axis, other.center);
		rad = Math.abs(Vector3f.dot(axis, other.X)) * other.bounds.x*other.scale + Math.abs(Vector3f.dot(axis, other.Y)) * other.bounds.y*other.scale
				+ Math.abs(Vector3f.dot(axis, other.Z)) * other.bounds.z*other.scale;

		float otherMin = dot - rad;
		float otherMax = dot + rad;

		if (selfMin > otherMax || selfMax < otherMin) {
			// Axis is not overlapping
			return null;
		}

		// Axis overlaps

		float d0 = selfMax - otherMin;
		float d1 = otherMax - selfMin;

		float overlap = (d0 < d1) ? -d0 : d1;
		float axisLengthSquared = axis.x * axis.x + axis.y * axis.y + axis.z * axis.z;
		float overlapSquared = (overlap * overlap) / axisLengthSquared;

		if (overlapSquared < overlapAxis.w) {
			overlapAxis.w = overlapSquared;
			Vector3f xyz = Vector3f.mul(axis, (overlap / axisLengthSquared));
			overlapAxis.x = xyz.x;
			overlapAxis.y = xyz.y;
			overlapAxis.z = xyz.z;
		}

		return overlapAxis;
	}
	
	private boolean axisTest(float rad, float p0, float p1) {
		float min = Math.min(p0, p1);
		float max = Math.max(p0, p1);
		if (min > rad || max < -rad) return true;
		
		//float d0 = max - (-rad);
		//float d1 = rad - min;
		//float o = (d0 < d1) ? d0 : d1;
		
		return false;
	}

	/**
	 * Checks an overlap between a triangle's plane and an AABB.
	 * 
	 * @param normal
	 *            - the triangle's normal.
	 * @param d
	 *            - plane equation (normal.x + d = 0)
	 * @param bounds
	 *            - the bounds of the AABB
	 *            
	 * @returns true is overlapping, false otherwise
	 */
	private boolean planeBoxOverlap(Vector3f normal, float d,
			Vector3f bounds) {
		// vmin is the AABB's bounds, with some axis flipped depending on the direction of the normal
		Vector3f vmin = new Vector3f((normal.x > 0.0) ? -bounds.x : +bounds.x,
				(normal.y > 0.0) ? -bounds.y : +bounds.y,
				(normal.z > 0.0) ? -bounds.z : +bounds.z);

		float dist = Vector3f.dot(normal, vmin) + d;
		if (dist > 0.0)
			return false;
		Vector3f vmax = Vector3f.negate(vmin);
		if (Vector3f.dot(normal, vmax) + d >= 0.0) {

			if (overlap > -dist) {
				overlap = -dist;
			}
			return true;
		}
		return false;
	}
	
	private boolean directionTest(float a, float b, float c, float bounds) {
		float min = Math.min(Math.min(a, b), c);
		float max = Math.max(Math.max(a, b), c);
		if (min > bounds || max < -bounds) return true;
		
		float d0 = max - (-bounds);
		float d1 = bounds - min;
		float o = (d0 < d1) ? d0 : d1;

		if (overlap > o) {
			overlap = o;
		}
		
		return false;
	}

	public boolean intersects(Polygon tri) {
		overlap = 9999;
		// Use separating axis theorem to test overlap between triangle and box.
		// Need to test for overlap in these directions:
		// 1) the {x,y,z}-directions (actually, since we use the AABB of the
		// triangle we do not even need to test these)
		// 2) normal of the triangle
		// 3) crossproduct(edge from triangle, {x,y,z}-direction). This gives
		// 3x3=9 more tests.

		// Move everything so that the boxcenter is in (0,0,0).
		Vector3f v0 = Vector3f.sub(tri.p1, center);
		Vector3f v1 = Vector3f.sub(tri.p2, center);
		Vector3f v2 = Vector3f.sub(tri.p3, center);

		// Bullet 3:
		// Test the 9 tests first (this was faster).

		Vector3f e0 = Vector3f.sub(v1, v0);
		Vector3f e1 = Vector3f.sub(v2, v1);
		Vector3f e2 = Vector3f.sub(v0, v2);
		
		
		Vector3f ea = new Vector3f();
		Vector3f e_v0 = new Vector3f();
		Vector3f e_v1 = new Vector3f();
		Vector3f e_v2 = new Vector3f();
			
		// EDGE 0
		
		ea = Vector3f.abs(e0);
		e_v0 = Vector3f.cross(e0, v0);
		e_v1 = Vector3f.cross(e0, v1);
		e_v2 = Vector3f.cross(e0, v2);

		if (axisTest(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v2.x))
			return false; // X
		if (axisTest(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v2.y))
			return false; // Y
		if (axisTest(ea.y * bounds.x + ea.x * bounds.y, e_v1.z, e_v2.z))
			return false; // Z

		// EDGE 1
		
		ea = Vector3f.abs(e1);
		e_v0 = Vector3f.cross(e1, v0);
		e_v1 = Vector3f.cross(e1, v1);
		e_v2 = Vector3f.cross(e1, v2);

		if (axisTest(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v2.x))
			return false;
		if (axisTest(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v2.y))
			return false;
		if (axisTest(ea.y * bounds.x + ea.x * bounds.y, e_v0.z, e_v1.z))
			return false;

		// EDGE 2
		
		ea = Vector3f.abs(e2);
		e_v0 = Vector3f.cross(e2, v0);
		e_v1 = Vector3f.cross(e2, v1);
		e_v2 = Vector3f.cross(e2, v2);

		if (axisTest(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v1.x))
			return false;
		
		if (axisTest(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v1.y))
			return false;
		if (axisTest(ea.y * bounds.x + ea.x * bounds.y, e_v1.z, e_v2.z))
			return false;
		
		// Bullet 1:
		// First test overlap in the {x,y,z}-directions.
		// Find min, max of the triangle each direction, and test for overlap in
		// that
		// direction -- this is equivalent to testing a minimal AABB around the
		// triangle against the AABB.
		if (directionTest(v0.x, v1.x, v2.x, bounds.x))
			return false; // Test in X-direction.
		
		if (directionTest(v0.y, v1.y, v2.y, bounds.y))
			return false; // Test in Y-direction.
		
		if (directionTest(v0.z, v1.z, v2.z, bounds.z))
			return false; // Test in Z-direction.
		
		// Bullet 2:
		// Test if the box intersects the plane of the triangle. Compute plane
		// equation of triangle: normal*x+d=0.
		Vector3f normal = tri.normal;
		float d = -Vector3f.dot(normal, v0); // plane eq: normal.x+d=0
		if (!planeBoxOverlap(normal, d, bounds))
			return false;
			
		return true; // box and triangle overlaps
	}
	
	public boolean intersection(Vector3f normal, float d) {
		Vector3f vmin = new Vector3f(
				(normal.x > 0.0) ? -bounds.x : bounds.x,
				(normal.y > 0.0) ? -bounds.y : bounds.y,
				(normal.z > 0.0) ? -bounds.z : bounds.z);

		float dist = Vector3f.dot(normal, vmin) + d;
		if (dist > 0.0) return false;
		Vector3f vmax = Vector3f.negate(vmin);
		if (Vector3f.dot(normal, vmax) + d >= 0f) return true;
		
		return false;
	}

	public void setPosition(float x, float y, float z) {
		center.set(x, y, z);
	}

	public void setPosition(Vector3f position) {
		setPosition(position.x, position.y, position.z);
	}

	public void setRotation(float rx, float ry, float rz) {
		this.yaw = rx;
		this.pitch = ry;
		this.roll = rz;
		Matrix4f matrix = new Matrix4f();

		matrix.rotateZ(rz);
		matrix.rotateY(ry);
		matrix.rotateX(rx);

		X.set(Vector3f.X_AXIS);
		Y.set(Vector3f.Y_AXIS);
		Z.set(Vector3f.Z_AXIS);

		matrix.transform(X);
		matrix.transform(Y);
		matrix.transform(Z);
	}

	public boolean containsPoint(float x, float z) {
		if (x > center.x-bounds.x*scale && x < center.x+bounds.x*scale
		&&  z > center.z-bounds.z*scale && z < center.z+bounds.z*scale) {
			return true;
		}
		return false;
	}

	public void transform(Vector3f translation, Quaternion rotation, float scale) {
		center.add(translation);
		Vector3f.rotateVector(X, rotation);
		Vector3f.rotateVector(Y, rotation);
		Vector3f.rotateVector(Z, rotation);
		bounds.mul(scale);
	}

	public Vector3f getCenter() {
		return center;
	}
	
	public Vector3f getBounds() {
		return bounds;
	}
}
