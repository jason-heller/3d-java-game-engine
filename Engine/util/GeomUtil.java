package util;

import org.joml.Vector3f;

public class GeomUtil {
	/** gets the distance between a point and an edge (line segment) in 3D space. Note that this wont work for edges facing directly upwards (0,1,0)
	 * @param point the point to project
	 * @param edgeStart first point of the edge
	 * @param edgeEnd second point of the edge
	 * @return the distance of the point to the edge, or NaN if the point cannot be projected onto the edge
	 */
	public static float pointDistanceToEdge(Vector3f point, Vector3f edgeStart, Vector3f edgeEnd) {
		Vector3f toStart = Vector3f.sub(edgeStart,  point);
		Vector3f toEnd = Vector3f.sub(edgeEnd, point);
		
		Vector3f edge = Vector3f.sub(edgeEnd, edgeStart);
		Vector3f ortho = Vector3f.cross(edge, Vector3f.Y_AXIS);
		
		float dot = Vector3f.dot(toStart, ortho);
		
		float signStart = Vector3f.dot(toStart, edge);
		float signEnd = -Vector3f.dot(toEnd, edge);
		
		if (Math.signum(signStart) != Math.signum(signEnd))
			return Float.NaN;
		
		return dot * dot / ortho.lengthSquared();
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
		
		Vector3f magnitude = Vector3f.mul(lineNormal, signedDist);
		return Vector3f.add(lineOrigin, magnitude);
	}
}