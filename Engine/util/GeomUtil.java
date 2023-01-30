package util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GeomUtil {
	/** gets the distance between a point and an edge (line segment) in 3D space. Note that this wont work for edges facing directly upwards (0,1,0)
	 * @param point the point to project
	 * @param edgeStart first point of the edge
	 * @param edgeEnd second point of the edge
	 * @return the distance of the point to the edge, or NaN if the point cannot be projected onto the edge
	 */
	public static float pointDistanceToEdge(Vector3f point, Vector3f edgeStart, Vector3f edgeEnd) {
		Vector3f edge = Vectors.sub(edgeEnd, edgeStart);
		Vector3f toStart = Vectors.sub(edgeStart, point);
		Vector3f toEnd = Vectors.sub(edgeEnd, point);

		float signStart = toStart.dot(edge);
		float signEnd = -toEnd.dot(edge);

		if (Math.signum(signStart) != Math.signum(signEnd))
			return Float.NaN;

		float lineDist = edge.lengthSquared();
		float t = ((point.x - edgeStart.x) * (edgeEnd.x - edgeStart.x)
				+ (point.y - edgeStart.y) * (edgeEnd.y - edgeStart.y)
				+ (point.z - edgeStart.z) * (edgeEnd.z - edgeStart.z)) / lineDist;
		
		t = Math.min(Math.max(t, 0f), 1f);
		Vector3f P = new Vector3f(edgeStart.x + t * (edgeEnd.x - edgeStart.x),
				edgeStart.y + t * (edgeEnd.y - edgeStart.y), edgeStart.z + t * (edgeEnd.z - edgeStart.z));
		Vector3f toLine = Vectors.sub(point, P);
		return toLine.length();
	}
	
	public static Vector3f getOrthogonal(Vector3f v) {
		if (v.z != 0f)
			return new Vector3f(1f, 1f, -(v.x + v.y) / v.z);
		
		if (v.z != 0f)
			return new Vector3f(1f, -(v.x + v.z) / v.y, 1f);
		
		return new Vector3f(-(v.y + v.z) / v.x, 1f, 1f);
	}

	/** Finds the projection of a point onto an infinite line.
	 * @param point The point to project
	 * @param lineOrigin a point on the line
	 * @param lineNormal the lines direction relative to the origin
	 * @return the point projected onto the line
	 */
	public static Vector3f projectPointOntoLine(Vector3f point, Vector3f lineOrigin, Vector3f lineNormal) {
		float planeDist = lineNormal.dot(lineOrigin);
		float signedDist = point.dot(lineNormal) - planeDist;
		
		Vector3f magnitude = Vectors.mul(lineNormal, signedDist);
		return Vectors.add(lineOrigin, magnitude);
	}

	/**
     * Set <code>this</code> quaternion to a rotation that rotates the <code>fromDir</code> vector to point along <code>toDir</code>.
     * <p>
     * Since there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/1171849/finding-quaternion-representing-the-rotation-from-one-vector-to-another#answer-1171995">stackoverflow.com</a>
     * 
     * @param fromDirX
     *              the x-coordinate of the direction to rotate into the destination direction
     * @param fromDirY
     *              the y-coordinate of the direction to rotate into the destination direction
     * @param fromDirZ
     *              the z-coordinate of the direction to rotate into the destination direction
     * @param toDirX
     *              the x-coordinate of the direction to rotate to
     * @param toDirY
     *              the y-coordinate of the direction to rotate to
     * @param toDirZ
     *              the z-coordinate of the direction to rotate to
     * @return this
     */
	@Deprecated
    public static Quaternionf rotationTo(float fromDirX, float fromDirY, float fromDirZ, float toDirX, float toDirY, float toDirZ) {
    	
    	Quaternionf q = new Quaternionf();
        float fn = 1f/(float)Math.sqrt(Math.fma(fromDirX, fromDirX, Math.fma(fromDirY, fromDirY, fromDirZ * fromDirZ)));
        float tn = 1f/(float)Math.sqrt(Math.fma(toDirX, toDirX, Math.fma(toDirY, toDirY, toDirZ * toDirZ)));
        float fx = fromDirX * fn, fy = fromDirY * fn, fz = fromDirZ * fn;
        float tx = toDirX * tn, ty = toDirY * tn, tz = toDirZ * tn;
        float dot = fx * tx + fy * ty + fz * tz;
        float x, y, z, w;
        if (dot < -1.0f + 1E-6f) {
            x = fy;
            y = -fx;
            z = 0.0f;
            w = 0.0f;
            if (x * x + y * y == 0.0f) {
                x = 0.0f;
                y = fz;
                z = -fy;
                w = 0.0f;
            }
            q.x = x;
            q.y = y;
            q.z = z;
            q.w = 0;
        } else {
            float sd2 = (float)Math.sqrt((1.0f + dot) * 2.0f);
            float isd2 = 1.0f / sd2;
            float cx = fy * tz - fz * ty;
            float cy = fz * tx - fx * tz;
            float cz = fx * ty - fy * tx;
            x = cx * isd2;
            y = cy * isd2;
            z = cz * isd2;
            w = sd2 * 0.5f;
            float n2 = 1f/(float)Math.sqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
            q.x = x * n2;
            q.y = y * n2;
            q.z = z * n2;
            q.w = w * n2;
        }
        return q;
    }
}