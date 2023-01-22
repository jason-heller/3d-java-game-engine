package util;

import org.joml.Matrix3f;
import org.joml.Quaternion;
import org.joml.Vector3f;

import dev.cmd.Console;

public class GeomUtil {
	/** gets the distance between a point and an edge (line segment) in 3D space. Note that this wont work for edges facing directly upwards (0,1,0)
	 * @param point the point to project
	 * @param edgeStart first point of the edge
	 * @param edgeEnd second point of the edge
	 * @return the distance of the point to the edge, or NaN if the point cannot be projected onto the edge
	 */
	public static float pointDistanceToEdge(Vector3f point, Vector3f edgeStart, Vector3f edgeEnd) {
		Vector3f edge = Vector3f.sub(edgeEnd, edgeStart);
		Vector3f toStart = Vector3f.sub(edgeStart, point);
		Vector3f toEnd = Vector3f.sub(edgeEnd, point);

		float signStart = Vector3f.dot(toStart, edge);
		float signEnd = -Vector3f.dot(toEnd, edge);

		if (Math.signum(signStart) != Math.signum(signEnd))
			return Float.NaN;

		float lineDist = edge.lengthSquared();
		float t = ((point.x - edgeStart.x) * (edgeEnd.x - edgeStart.x)
				+ (point.y - edgeStart.y) * (edgeEnd.y - edgeStart.y)
				+ (point.z - edgeStart.z) * (edgeEnd.z - edgeStart.z)) / lineDist;
		
		t = Math.min(Math.max(t, 0f), 1f);
		Vector3f P = new Vector3f(edgeStart.x + t * (edgeEnd.x - edgeStart.x),
				edgeStart.y + t * (edgeEnd.y - edgeStart.y), edgeStart.z + t * (edgeEnd.z - edgeStart.z));
		Vector3f toLine = Vector3f.sub(point, P);
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
		
		Vector3f magnitude = Vector3f.mul(lineNormal, signedDist);
		return Vector3f.add(lineOrigin, magnitude);
	}
	
	public static Quaternion normalToQuaternion(Vector3f vector, Vector3f up) {
		Vector3f secondAxis = Vector3f.cross(up, vector);
		Vector3f thirdAxis = Vector3f.cross(vector, secondAxis);

		float m00 = secondAxis.x;
		float m01 = secondAxis.y;
		float m02 = secondAxis.z;
		float m10 = thirdAxis.x;
		float m11 = thirdAxis.y;
		float m12 = thirdAxis.z;
		float m20 = vector.x;
		float m21 = vector.y;
		float m22 = vector.z;

		float num8 = (m00 + m11) + m22;
		Quaternion quaternion = new Quaternion();
		
		if (num8 > 0f) {
			float num = (float) Math.sqrt(num8 + 1f);
			quaternion.w = num * 0.5f;
			num = 0.5f / num;
			quaternion.x = (m12 - m21) * num;
			quaternion.y = (m20 - m02) * num;
			quaternion.z = (m01 - m10) * num;
			return quaternion;
		}

		if ((m00 >= m11) && (m00 >= m22)) {
			float num7 = (float) Math.sqrt(((1f + m00) - m11) - m22);
			float num4 = 0.5f / num7;
			quaternion.x = 0.5f * num7;
			quaternion.y = (m01 + m10) * num4;
			quaternion.z = (m02 + m20) * num4;
			quaternion.w = (m12 - m21) * num4;
			return quaternion;
		}

		if (m11 > m22) {
			float num6 = (float) Math.sqrt(((1f + m11) - m00) - m22);
			float num3 = 0.5f / num6;
			quaternion.x = (m10 + m01) * num3;
			quaternion.y = 0.5f * num6;
			quaternion.z = (m21 + m12) * num3;
			quaternion.w = (m20 - m02) * num3;
			return quaternion;
		}

		float num5 = (float) Math.sqrt(((1f + m22) - m00) - m11);
		float num2 = 0.5f / num5;
		
		quaternion.x = (m20 + m02) * num2;
		quaternion.y = (m21 + m12) * num2;
		quaternion.z = 0.5f * num5;
		quaternion.w = (m01 - m10) * num2;
		return quaternion;
	}
	

	/*public static Vector3f quaternionToEuler(Quaternion q) {
		return new Vector3f(getQuatPitch(q), getQuatYaw(q), getQuatRoll(q));
	}
	
	public static float getQuatYaw(Quaternion q) {
		float t0 = 2f * ((q.w * q.z) + (q.x * q.y));
		float t1 = 1f - 2f * ((q.y * q.y) + (q.z * q.z));
		return (float)Math.toDegrees(Math.atan2(t0, t1));
	}
	
	public static float getQuatPitch(Quaternion q) {
		float t = 2f * ((q.w * q.y) - (q.z * q.x));
		t = (t > 1.0f) ? 1f : t;
		t = (t < -1.0f) ? -1f : t;
		return (float)Math.toDegrees(Math.asin(t));
	}
	
	public static float getQuatRoll(Quaternion q) {
		float t0 = 2f * ((q.w * q.x) + (q.y * q.z));
		float t1 = 1f - 2f * ((q.x * q.x) + (q.y * q.y));
		return (float)Math.toDegrees(Math.atan2(t0, t1));
	}*/
	
	public static void rotateAboutNormal(Vector3f euler, Vector3f normal, Vector3f up) {
		if (normal.equals(up))
			return;
		
		Vector3f xAxis = up.cross(normal).normalize();
		Vector3f yAxis = normal.cross(xAxis).normalize();

		// set orientation of model matrix
		Matrix3f m = new Matrix3f();
		m.m00 = xAxis.x;
		m.m10 = yAxis.x;
		m.m20 = normal.x;

		m.m01 = xAxis.y;
		m.m11 = yAxis.y;
		m.m21 = normal.y;

		m.m02 = xAxis.z;
		m.m12 = yAxis.z;
		m.m22 = normal.z;
		
		euler.mul(m);
	}
}