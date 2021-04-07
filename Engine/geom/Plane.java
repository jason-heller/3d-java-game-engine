package geom;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Plane {
	public static final byte IN_FRONT = 0x0, BEHIND = 0x1, COPLANAR = 0x2;

	public Vector3f normal;
	public float dist;

	public Plane() {
		this.normal = new Vector3f();
		this.dist = 0;
	}

	public Plane(Vector3f normal, float equation) {
		this.normal = normal;
		this.dist = equation;
	}

	public Plane(Vector3f origin, Vector3f normal) {
		this.normal = normal;
		dist = (normal.x * origin.x + normal.y * origin.y + normal.z * origin.z);
	}
	
	public Plane(Vector3f p1, Vector3f p2, Vector3f p3) {
		normal = Vector3f.cross(Vector3f.sub(p2, p1), Vector3f.sub(p3, p1)).normalize();
		dist = (normal.x * p1.x + normal.y * p1.y + normal.z * p1.z);
	}

	public byte classify(Vector3f point, float planeThickness) {
		final float fDist = Vector3f.dot(normal, point) - dist;
		if (fDist > planeThickness) {
			return IN_FRONT;
		}
		if (fDist < -planeThickness) {
			return BEHIND;
		}
		return COPLANAR;
	}

	public void set(float a, float b, float c, float d) {
		normal.set(a, b, c);

		final float len = normal.length();
		normal.div(len);

		dist = d / len;
	}

	public void set(Vector3f p1, Vector3f p2, Vector3f p3) {
		this.normal = Vector3f.sub(p2, p1).cross(Vector3f.sub(p3, p1));
		dist = normal.x * p1.x + normal.y * p1.y + normal.z * p1.z;
	}
	
	public Vector3f projectPoint(Vector3f point, Vector3f norm, float dist) {
		return Vector3f.sub(point, Vector3f.mul(norm, point.dot(norm) + dist));
	}
	
	public boolean isFrontFacingTo(Vector3f direction) {
		final double dot = normal.dot(direction);
		return dot <= 0;
	}

	public Vector3f projectPoint(Vector3f point) {
		return Vector3f.sub(point, Vector3f.mul(normal, (float) signedDistanceTo(point)));
	}

	public float collide(Vector3f rayOrigin, Vector3f rayDirection) {
		final float dp = normal.dot(rayDirection);
		if (Math.abs(dp) < .00001f) {
			return Float.NaN;
		}

		return (dist - normal.dot(rayOrigin)) / dp;
	}

	public float signedDistanceTo(Vector3f point) {
		return point.dot(normal) - dist;
	}

	public void translate(Vector3f offset) {
		final Vector3f newPt = Vector3f.add(offset, Vector3f.mul(normal, dist));
		dist = normal.x * newPt.x + normal.y * newPt.y + normal.z * newPt.z;
	}

	public boolean intersects(AxisAlignedBBox aabb) {
		final Vector3f center = aabb.getCenter();
		final Vector3f bounds = aabb.getBounds();
		final Vector3f tl = Vector3f.sub(center, bounds);
		final Vector3f br = Vector3f.add(center, bounds);
		
		float sign = Math.signum(tl.dot(normal) + dist);
		if (Math.signum(br.dot(normal) + dist) != sign) return true;
		
		if (Math.signum(new Vector3f(tl.x, tl.y, br.z).dot(normal) + dist) != sign) return true;
		if (Math.signum(new Vector3f(br.x, tl.y, tl.z).dot(normal) + dist) != sign) return true;
		if (Math.signum(new Vector3f(br.x, tl.y, br.z).dot(normal) + dist) != sign) return true;
		
		if (Math.signum(new Vector3f(br.x, br.y, tl.z).dot(normal) + dist) != sign) return true;
		if (Math.signum(new Vector3f(tl.x, br.y, br.z).dot(normal) + dist) != sign) return true;
		if (Math.signum(new Vector3f(tl.x, br.y, tl.z).dot(normal) + dist) != sign) return true;

		return false;
	}
}
