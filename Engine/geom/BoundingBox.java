package geom;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import util.Vectors;

public class BoundingBox {

	public Vector3f halfSize;
	public Vector3f center;
	
	public Vector3f X, Y, Z;
	
	private Vector3f xBounds = new Vector3f();
	private Vector3f yBounds = new Vector3f();
	private Vector3f zBounds = new Vector3f();
	
	private Vector3f intersectionAxis;
	private float intersectionDepth;

	public BoundingBox(float x, float y, float z, float w, float h, float l) {
		this.center = new Vector3f(x, y, z);
		this.X = new Vector3f(Vectors.POSITIVE_X);
		this.Y = new Vector3f(Vectors.POSITIVE_Y);
		this.Z = new Vector3f(Vectors.POSITIVE_Z);
		this.halfSize = new Vector3f(w, h, l);
		
		xBounds.set(X).mul(halfSize.x);
		yBounds.set(Y).mul(halfSize.y);
		zBounds.set(Z).mul(halfSize.z);
	}

	public BoundingBox(Vector3f center, float w, float h, float l) {
		this.center = center;
		this.X = new Vector3f(Vectors.POSITIVE_X);
		this.Y = new Vector3f(Vectors.POSITIVE_Y);
		this.Z = new Vector3f(Vectors.POSITIVE_Z);
		this.halfSize = new Vector3f(w, h, l);
		
		xBounds.set(X).mul(halfSize.x);
		yBounds.set(Y).mul(halfSize.y);
		zBounds.set(Z).mul(halfSize.z);
	}

	public BoundingBox(Vector3f center, Vector3f max, Vector3f min) {
		this(center, Vectors.sub(max, min));

	}
	
	public BoundingBox(Vector3f center, Vector3f bounds) {
		this.halfSize = bounds;
		this.center = center;
		this.X = new Vector3f(Vectors.POSITIVE_X);
		this.Y = new Vector3f(Vectors.POSITIVE_Y);
		this.Z = new Vector3f(Vectors.POSITIVE_Z);
		
		xBounds.set(X).mul(halfSize.x);
		yBounds.set(Y).mul(halfSize.y);
		zBounds.set(Z).mul(halfSize.z);

	}
	
	public boolean intersects(AABB box) {
		return intersects(new BoundingBox(box.getCenter(), box.getBounds()));
	}
	
	public boolean intersects(BoundingBox box) {
		intersectionDepth = Float.POSITIVE_INFINITY;
		Vector3f range = Vectors.sub(box.getCenter(), center);
		
		return !(
				isSeparated(range, X, box) ||
				isSeparated(range, Y, box) ||
				isSeparated(range, Z, box) ||
				isSeparated(range, box.X, box) ||
				isSeparated(range, box.Y, box) ||
				isSeparated(range, box.Z, box) ||
				isSeparated(range, cross(X, box.X), box) ||
				isSeparated(range, cross(X, box.Y), box) ||
				isSeparated(range, cross(X, box.Z), box) ||
				isSeparated(range, cross(Y, box.X), box) ||
				isSeparated(range, cross(Y, box.Y), box) ||
				isSeparated(range, cross(Y, box.Z), box) ||
				isSeparated(range, cross(Z, box.X), box) ||
				isSeparated(range, cross(Z, box.Y), box) ||
				isSeparated(range, cross(Z, box.Z), box)
		);
	}
	
	public boolean intersects(Vector3f[] convexVertices, Vector3f planeNormal) {
		intersectionDepth = Float.POSITIVE_INFINITY;
		final int len = convexVertices.length / 2;
		
		Vector3f[] axisList = new Vector3f[len + 4];
		
		Vector3f[] boxVertices = new Vector3f[] {
				new Vector3f(center).add(xBounds).add(yBounds).add(zBounds),
				new Vector3f(center).sub(xBounds).add(yBounds).add(zBounds),
				new Vector3f(center).add(xBounds).add(yBounds).sub(zBounds),
				new Vector3f(center).sub(xBounds).add(yBounds).sub(zBounds),
				
				new Vector3f(center).add(xBounds).sub(yBounds).add(zBounds),
				new Vector3f(center).sub(xBounds).sub(yBounds).add(zBounds),
				new Vector3f(center).add(xBounds).sub(yBounds).sub(zBounds),
				new Vector3f(center).sub(xBounds).sub(yBounds).sub(zBounds)
		};
		
		axisList[0] = planeNormal;
		axisList[1] = X;
		axisList[2] = Y;
		axisList[3] = Z;
		
		for(int i = 0; i < len; i++) {
			Vector3f v1 = convexVertices[i * 2];
			Vector3f v2 = convexVertices[i * 2 + 1];
			
			axisList[i + 4] = new Vector3f(v2).sub(v1).cross(planeNormal).normalize();
		}
		
		for (int i = 0; i < axisList.length; i++) {
			if (isSeparated(boxVertices, convexVertices, axisList[i]))
				return false;
		}

		return true;
	}
	
	public boolean intersects(Plane plane) {
		final Vector3f normal = plane.normal;
		
		float length = halfSize.x * Math.abs(normal.dot(X)) +
				halfSize.y * Math.abs(normal.dot(Y)) +
				halfSize.z * Math.abs(normal.dot(Z));
		
		float dp = normal.dot(center);
		float dist = Math.abs(dp - plane.dist);
		
		return dist <= length;
	}
	
	private boolean isSeparated(Vector3f range, Vector3f axis, BoundingBox box) {
		float minOverlap = Math.abs(range.dot(axis));
		
		float separation = 
				Math.abs(xBounds.dot(axis)) +
				Math.abs(yBounds.dot(axis)) +
				Math.abs(zBounds.dot(axis)) +
				Math.abs(box.getBoundsX().dot(axis)) +
				Math.abs(box.getBoundsY().dot(axis)) +
				Math.abs(box.getBoundsZ().dot(axis));
		
		if (minOverlap > separation) 
			return true;
		
		mtv(axis, separation - minOverlap);
		
		return false;
	}
	
	private boolean isSeparated(Vector3f[] polygonVertices1, Vector3f[] polygonVertices2, Vector3f axis) {
		float min1 = Float.POSITIVE_INFINITY, max1 = Float.NEGATIVE_INFINITY;
		float min2 = Float.POSITIVE_INFINITY, max2 = Float.NEGATIVE_INFINITY;

		for(Vector3f vertex : polygonVertices1) {
			float projection = vertex.dot(axis);
			
			min1 = Math.min(min1, projection);
			max1 = Math.max(max1, projection);
		}
		
		for(Vector3f vertex : polygonVertices2) {
			float projection = vertex.dot(axis);
			
			min2 = Math.min(min2, projection);
			max2 = Math.max(max2, projection);
		}
		
		if (max1 < min2 || max2 < min1)
			return true;
		
		float smallestOverlap = Math.min(max2 - min1, max1 - min2);
		//float escapeDist = smallestOverlap / axis.dot(axis);
		
		mtv(axis, smallestOverlap);
		
		return false;
	}
	
	private void mtv(Vector3f axis, float depth) {
		if (depth < intersectionDepth) {
			intersectionDepth = depth;
			intersectionAxis = axis;
		}
	}

	public boolean collide(Vector3f origin, Vector3f direction) {
		intersectionDepth = Float.POSITIVE_INFINITY;
		float min = Float.NEGATIVE_INFINITY;
		float max = Float.POSITIVE_INFINITY;
		
		Vector3f delta = Vectors.sub(center, origin);
		float[] range;
		
		// X
		range = axisTest(delta, direction, X, halfSize.x);
		
		max = Math.min(max, range[1]);
		min = Math.max(min, range[0]);
		
		if (max < min )
			return false;
		
		// Y
		range = axisTest(delta, direction, X, halfSize.x);
		
		max = Math.min(max, range[1]);
		min = Math.max(min, range[0]);
		
		if (max < min )
			return false;
		
		// Z
		range = axisTest(delta, direction, X, halfSize.x);
		
		max = Math.min(max, range[1]);
		min = Math.max(min, range[0]);
		
		if (max < min )
			return false;
		
		return true;
	}

	private float[] axisTest(Vector3f delta, Vector3f direction, Vector3f axis, float magnitude) {
		float e = axis.dot(delta);
		float f = direction.dot(axis);
		
		float t1 = (e - magnitude) / f;
		float t2 = (e + magnitude) / f;
		
		if (t1 > t2) {
			float w = t1;
			t2 = t1;
			t1 = w;
		}
		
		return new float[] {t1, t2};
	}

	public void setRotation(Quaternionf rot) {

		X.set(Vectors.POSITIVE_X).rotate(rot);
		Y.set(Vectors.POSITIVE_Y).rotate(rot);
		Z.set(Vectors.POSITIVE_Z).rotate(rot);
		
		xBounds.set(X).mul(halfSize.x);
		yBounds.set(Y).mul(halfSize.y);
		zBounds.set(Z).mul(halfSize.z);
	}
	
	public Vector3f getBoundsX() {
		return xBounds;
	}
	
	public Vector3f getBoundsY() {
		return yBounds;
	}
	
	public Vector3f getBoundsZ() {
		return zBounds;
	}
	
	public void setPosition(float x, float y, float z) {
		center.set(x, y, z);
	}

	public void setPosition(Vector3f position) {
		setPosition(position.x, position.y, position.z);
	}
	
	public Vector3f getCenter() {
		return center;
	}
	
	public Vector3f getHalfSize() {
		return halfSize;
	}
	
	public float getWidth() {
		return halfSize.x;
	}
	
	public float getHeight() {
		return halfSize.y;
	}
	
	public float getLength() {
		return halfSize.z;
	}
	
	public float getIntersectionDepth() {
		return this.intersectionDepth;
	}
	
	public Vector3f getIntersectionAxis() {
		return this.intersectionAxis;
	}
	
	private Vector3f cross(Vector3f axis1, Vector3f axis2) {
		return new Vector3f(axis1).cross(axis2);
	}
}
