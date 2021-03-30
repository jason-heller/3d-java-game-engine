package geom;

import org.joml.Vector3f;

import dev.Console;

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
		Vector3f lb = new Vector3f(getX()-getWidth(), getY()-getHeight(), getZ()-getLength());
		Vector3f rt = new Vector3f(getX()+getWidth(), getY()+getHeight(), getZ()+getLength());
		float t = Float.NaN;
		
		float t1 = (lb.x - org.x)*dirfrac.x;
		float t2 = (rt.x - org.x)*dirfrac.x;
		float t3 = (lb.y - org.y)*dirfrac.y;
		float t4 = (rt.y - org.y)*dirfrac.y;
		float t5 = (lb.z - org.z)*dirfrac.z;
		float t6 = (rt.z - org.z)*dirfrac.z;

		float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0) {
		    t = tmax;
		    return Float.NaN;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax) {
		    t = tmax;
		    return Float.NaN;
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
		float dist = Math.abs(center.x - other.center.x) - bounds.x;
		float minDist = dist;
		float dir = Math.signum(center.x - other.center.x);
		
		dist = Math.abs(center.y - other.center.y) - bounds.y;
		if (dist < minDist) {
			dir = Math.signum(center.y - other.center.y);
			minDist = dist;
			index = 1;
		}
		
		dist = Math.abs(center.z - other.center.z) - bounds.z;
		if (dist < minDist) {
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
}