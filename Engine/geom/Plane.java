package geom;

import org.joml.Vector3f;

import util.Vectors;

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
		normal = Vectors.cross(Vectors.sub(p2, p1), Vectors.sub(p3, p1)).normalize();
		dist = (normal.x * p1.x + normal.y * p1.y + normal.z * p1.z);
	}

	public byte classify(Vector3f point, float planeThickness) {
		final float fDist = normal.dot(point) - dist;
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
		this.normal = Vectors.sub(p2, p1).cross(Vectors.sub(p3, p1));
		dist = normal.x * p1.x + normal.y * p1.y + normal.z * p1.z;
	}
	
	public static Vector3f projectPoint(Vector3f point, Vector3f norm, float dist) {
		return Vectors.sub(point, Vectors.mul(norm, point.dot(norm) + dist));
	}
	
	public boolean isFrontFacingTo(Vector3f direction) {
		final double dot = normal.dot(direction);
		return dot <= 0;
	}

	public Vector3f projectPoint(Vector3f point) {
		return Vectors.sub(point, Vectors.mul(normal, (float) signedDistanceTo(point)));
	}

	public Vector3f raycastPoint(Vector3f org, Vector3f dir) {
		final float dp = normal.dot(dir);
		if (Math.abs(dp) < .00001f)
			return org;
			
		float t = (dist - normal.dot(org)) / dp;
		return Vectors.add(org, Vectors.mul(dir, t));
	}
	
	public float raycast(Vector3f org, Vector3f dir) {
		final float dp = normal.dot(dir);
		if (Math.abs(dp) < .00001f)
			return Float.NaN;

		float t = (dist - normal.dot(org)) / dp;
		return t >= 0 ? t : Float.NaN;
	}

	public float signedDistanceTo(Vector3f point) {
		return point.dot(normal) - dist;
	}

	public void translate(Vector3f offset) {
		final Vector3f newPt = Vectors.add(offset, Vectors.mul(normal, dist));
		dist = normal.x * newPt.x + normal.y * newPt.y + normal.z * newPt.z;
	}

	public boolean intersects(AABB aabb) {
		final Vector3f center = aabb.getCenter();
		final Vector3f bounds = aabb.getBounds();
		final Vector3f tl = Vectors.sub(center, bounds);
		final Vector3f br = Vectors.add(center, bounds);
		
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
