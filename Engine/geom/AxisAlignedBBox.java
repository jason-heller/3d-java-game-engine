package geom;

import org.joml.Vector3f;

public class AxisAlignedBBox {
	private Vector3f center;
	private Vector3f bounds;

	public AxisAlignedBBox(Vector3f center, Vector3f bounds) {
		this.center = center;
		this.bounds = bounds;
	}

	public AxisAlignedBBox(float x, float y, float z, float wid, float hei, float len) {
		this(new Vector3f(x, y, z), new Vector3f(wid, hei, len));
	}

	public void setCenter(float x, float y, float z) {
		this.center.set(x,y,z);
	}
	
	public void setCenter(Vector3f vec) {
		this.center.set(vec);
	}
	
	public void setBounds(float w, float h, float l) {
		this.bounds.set(w, h, l);
	}

	public float getX() {
		return center.x;
	}
	
	public float getY() {
		return center.y;
	}
	
	public float getZ() {
		return center.z;
	}
	
	public float getWidth() {
		return bounds.x;
	}
	
	public float getHeight() {
		return bounds.y;
	}
	
	public float getLength() {
		return bounds.z;
	}
	
	public Vector3f getCenter() {
		return center;
	}

	public Vector3f getBounds() {
		return bounds;
	}
	
	public String toString() {
		return center.toString() + " " + bounds.toString();
	}
	
	public float collide(Vector3f org, Vector3f dir) {
		// r.dir is unit direction vector of ray
		Vector3f dirfrac = new Vector3f();
		dirfrac.x = 1.0f / dir.x;
		dirfrac.y = 1.0f / dir.y;
		dirfrac.z = 1.0f / dir.z;
		// lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
		// r.org is origin of ray
		Vector3f lb = new Vector3f(getX() - getWidth(), getY() - getHeight(), getZ() - getLength());
		Vector3f rt = new Vector3f(getX() + getWidth(), getY() + getHeight(), getZ() + getLength());
		float t = Float.NaN;

		float t1 = (lb.x - org.x) * dirfrac.x;
		float t2 = (rt.x - org.x) * dirfrac.x;
		float t3 = (lb.y - org.y) * dirfrac.y;
		float t4 = (rt.y - org.y) * dirfrac.y;
		float t5 = (lb.z - org.z) * dirfrac.z;
		float t6 = (rt.z - org.z) * dirfrac.z;

		float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0) {
			t = tmax;
			return Float.POSITIVE_INFINITY;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax) {
			t = tmax;
			return Float.POSITIVE_INFINITY;
		}

		t = tmin;
		return t;
	}
	
	public boolean intersects(AxisAlignedBBox box) {
		if ( Math.abs(getCenter().x - box.getCenter().x) > (getBounds().x + box.getBounds().x) ) return false;
	    if ( Math.abs(getCenter().y - box.getCenter().y) > (getBounds().y + box.getBounds().y) ) return false;
	    if ( Math.abs(getCenter().z - box.getCenter().z) > (getBounds().z + box.getBounds().z) ) return false;
	 
	    // We have an overlap
	    return true;
	}
	
	public MTV collide(AxisAlignedBBox other) {
		if (!intersects(other)) {
			return null;
		}
		
		// TODO: Fix this and write not shit code. This is shit code.
		int index = 0;
		float dist = Math.abs(center.x - other.center.x);
		float minDist = dist;
		float dir = Math.signum(center.x - other.center.x);
		
		dist = Math.abs(center.y - other.center.y);
		if (dist > minDist) {
			dir = Math.signum(center.y - other.center.y);
			minDist = dist;
			index = 1;
		}
		
		dist = Math.abs(center.z - other.center.z);
		if (dist > minDist) {
			dir = Math.signum(center.z - other.center.z);
			minDist = dist;
			index = 2;
		}
		
		switch(index) {
		case 0:
			return new MTV(minDist, new Vector3f(dir, 0, 0));
		case 1:
			return new MTV(minDist, new Vector3f(0, dir, 0));
		default:
			return new MTV(minDist, new Vector3f(0, 0, dir));
		}
	}

	public boolean collide(Vector3f point) {
		return (point.x >= center.x - bounds.x && point.x <= center.x + bounds.x)
				&& (point.y >= center.y - bounds.y && point.y <= center.y + bounds.y)
				&& (point.z >= center.z - bounds.z && point.z <= center.z + bounds.z);
	}

	public MTV collide(Polygon tri) {
		final Vector3f tl = Vector3f.sub(center, bounds);
		final Vector3f br = Vector3f.add(center, bounds);
		MTV mtv = new MTV();

		final Vector3f[] facePoints = new Vector3f[] {tri.p1, tri.p2, tri.p3};

		final Vector3f[] boxPoints = new Vector3f[] { tl, new Vector3f(br.x, tl.y, tl.z),
				new Vector3f(tl.x, tl.y, br.z), new Vector3f(br.x, tl.y, br.z), br, new Vector3f(br.x, br.y, tl.z),
				new Vector3f(tl.x, br.y, br.z), new Vector3f(tl.x, br.y, tl.z) };

		Bounds faceBounds, boxBounds;

		final float[] boxMins = new float[] { tl.x, tl.y, tl.z };
		final float[] boxMaxs = new float[] { br.x, br.y, br.z };

		// Test face normals of AABB
		for (int i = 0; i < 3; i++) {
			faceBounds = project(facePoints, CollideUtils.axisPtrs[i]);
			if (faceBounds.max < boxMins[i] || faceBounds.min > boxMaxs[i])
				return null;
		}

		// Test face normal of face
		Vector3f normal = tri.normal;
		float faceOffset = normal.dot(facePoints[0]);
		boxBounds = project(boxPoints, normal);

		if (!mtv.testAxis(boxBounds.max, boxBounds.min, faceOffset, faceOffset, normal))
			return null;

		// Get all edges of face
		Vector3f[] faceEdges = new Vector3f[] {
				Vector3f.sub(tri.p2, tri.p1),
				Vector3f.sub(tri.p3, tri.p2),
				Vector3f.sub(tri.p1, tri.p3)
		};

		for (int i = 0; i < faceEdges.length; i++) {
			for (int j = 0; j < 3; j++) {
				Vector3f axis = faceEdges[i].cross(CollideUtils.axisPtrs[j]);
				
				if (axis.isZero())
					continue;
				
				boxBounds = project(boxPoints, axis);
				faceBounds = project(facePoints, axis);
				
				if (!mtv.testAxis(boxBounds.max, boxBounds.min, faceBounds.max, faceBounds.min, axis))
					return null;
			}
		}
		
		mtv.finish(facePoints[0], null);
		return mtv;
	}
	
	private static Bounds project(Vector3f[] points, Vector3f axis) {
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;

		for (Vector3f pt : points) {
			float val = axis.dot(pt);
			if (val < min)
				min = val;
			if (val > max)
				max = val;
		}

		return new Bounds(max, min);
	}
}