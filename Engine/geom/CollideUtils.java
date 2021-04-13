package geom;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import dev.Console;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;

/**
 * @author Jason Basically just a class to handle collisions with geometry that
 *         either consists of groups of existing geometry to have utilities
 *         useful for assisting collision checks. Probably has some stuff in
 *         common to MathUtils, that's probably refactor-worthy
 */
public class CollideUtils {

	private final static Vector3f[] axisPtrs = new Vector3f[] { Vector3f.X_AXIS, Vector3f.Y_AXIS, Vector3f.Z_AXIS};

	/**
	 * Gets the MTV between an axis aligned bounding box and a face defined within a
	 * architecture's BSP
	 * 
	 * @param -
	 *            vertices the vertices of the BSP
	 * @param -
	 *            edges the edges of the BSP
	 * @param -
	 *            surfEdges the surface edgfes of a BSP
	 * @param -
	 *            face the face of the BSP
	 * @param -
	 *            normal the face's normal
	 * @param -
	 *            box the bounding box to collide with
	 * @return The minimum translation vector of the collision, or null if no
	 *         collision occured
	 */
	public static MTV bspFaceBoxCollide(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, ArcFace face,
			Vector3f normal, AxisAlignedBBox box) {

		final Vector3f tl = Vector3f.sub(box.getCenter(), box.getBounds());
		final Vector3f br = Vector3f.add(box.getCenter(), box.getBounds());
		MTV mtv = new MTV();

		final Vector3f[] facePoints = new Vector3f[face.numEdges];
		for (int i = 0; i < facePoints.length; i++) {
			int id = face.firstEdge + i;
			int surfId = surfEdges[id];
			facePoints[i] = vertices[surfId > 0 ? edges[surfId].start : edges[-surfId].end];
		}

		final Vector3f[] boxPoints = new Vector3f[] { tl, new Vector3f(br.x, tl.y, tl.z),
				new Vector3f(tl.x, tl.y, br.z), new Vector3f(br.x, tl.y, br.z), br, new Vector3f(br.x, br.y, tl.z),
				new Vector3f(tl.x, br.y, br.z), new Vector3f(tl.x, br.y, tl.z) };

		Bounds faceBounds, boxBounds;

		final float[] boxMins = new float[] { tl.x, tl.y, tl.z };
		final float[] boxMaxs = new float[] { br.x, br.y, br.z };

		// Test face normals of AABB
		for (int i = 0; i < 3; i++) {
			faceBounds = project(facePoints, axisPtrs[i]);
			if (faceBounds.max < boxMins[i] || faceBounds.min > boxMaxs[i])
				return null;

			//if (!mtv.testAxis(faceBounds.max, faceBounds.min, boxMaxs[i], boxMins[i], axisPtrs[i]))
			//	return null;
		}

		// Test face normal of face
		float faceOffset = normal.dot(facePoints[0]);
		boxBounds = project(boxPoints, normal);
		//if (boxBounds.max < faceOffset || boxBounds.min > faceOffset)
		//	return null;

		if (!mtv.testAxis(boxBounds.max, boxBounds.min, faceOffset, faceOffset, normal))
			return null;

		// Get all edges of face
		Vector3f[] faceEdges = new Vector3f[face.numEdges];
		for (int i = 0; i < faceEdges.length; i++) {
			int id = face.firstEdge + i;
			//ArcEdge edge = edges[Math.abs(surfEdges[id])];
			//faceEdges[i] = Vector3f.sub(vertices[edge.end], vertices[edge.start]);

			int surf = surfEdges[id];
			if (surf >= 0) {
				faceEdges[i] = Vector3f.sub(vertices[edges[surf].end], vertices[edges[surf].start]);
			} else {
				faceEdges[i] = Vector3f.sub(vertices[edges[-surf].start], vertices[edges[-surf].end]);
			}

		}

		for (int i = 0; i < faceEdges.length; i++) {
			for (int j = 0; j < 3; j++) {
				Vector3f axis = faceEdges[i].cross(axisPtrs[j]);
				
				if (axis.isZero())
					continue;
				
				boxBounds = project(boxPoints, axis);
				faceBounds = project(facePoints, axis);
				
				//if (boxBounds.max < faceBounds.min || boxBounds.min > faceBounds.max)
				//	return null;

				if (!mtv.testAxis(boxBounds.max, boxBounds.min, faceBounds.max, faceBounds.min, axis))
					return null;
			}
		}
		
		mtv.finish(facePoints[0], face);
		return mtv;
	}

	/**
	 * @return
	 */
	public static MTV convexHullBoxCollide(Plane[] planes, AxisAlignedBBox box) {
		MTV mtv = new MTV();
		
		for (Plane plane : planes) {
			float dist = plane.signedDistanceTo(box.getCenter());
			Vector3f bounds = Vector3f.mul(box.getBounds(), plane.normal).abs();
			float boundsLen = bounds.x + bounds.y + bounds.z;
			if (dist > boundsLen) {
				return null;
			}
			
			if (boundsLen - dist < mtv.getDepth()) {
				mtv.setDepth(boundsLen - dist);
				mtv.setAxis(plane.normal);
				mtv.setPlane(plane);
			}

		}
		
		return mtv;
	}

	public static float convexPolygonRay(Bsp bsp, ArcFace face, Vector3f rayOrigin, Vector3f rayDir) {
		
		Vector3f[] vertices = bsp.vertices;
		ArcEdge[] edges = bsp.edges;
		int[] surfEdges = bsp.surfEdges;
		//Plane[] planes = bsp.planes;

		Plane plane = bsp.planes[face.planeId];
		Vector3f normal = plane.normal;
		float dist = plane.dist;

		float a = dist - normal.dot(rayOrigin);
		float b = normal.dot(rayDir);

		if (Math.abs(b) < .0001f)
			return Float.NaN;

		float t = a / b;
		if (t < 0.0)
			return Float.NaN;

		Vector3f pt = new Vector3f(rayOrigin);
		pt.add(Vector3f.mul(rayDir, t));
  
		int projAxis = 0;
		Vector3f nAbs = Vector3f.abs(normal);
		if (nAbs.y > nAbs.x && nAbs.y > nAbs.z) {
			projAxis = 1;
		} else if (nAbs.z > nAbs.x && nAbs.z > nAbs.y) {
			projAxis = 2;
		}

		int e1, e2;
		Vector3f v1, v2;
		Vector2f edge = new Vector2f(), affinePt = new Vector2f();
		int lastSide = 0;

		for (int i = 0; i < face.numEdges; i++) {
			int edgeId = face.firstEdge + i;
			if (surfEdges[edgeId] >= 0) {
				e1 = edges[surfEdges[edgeId]].start;
				e2 = edges[surfEdges[edgeId]].end;
			} else {
				e1 = edges[-surfEdges[edgeId]].end;
				e2 = edges[-surfEdges[edgeId]].start;
			}
			v1 = vertices[e1];
			v2 = vertices[e2];

			switch (projAxis) {
			case 0:
				edge.set(v2.y - v1.y, v2.z - v1.z);
				affinePt.set(pt.y - v1.y, pt.z - v1.z);
				break;
			case 1:
				edge.set(v2.x - v1.x, v2.z - v1.z);
				affinePt.set(pt.x - v1.x, pt.z - v1.z);
				break;
			default:
				edge.set(v2.x - v1.x, v2.y - v1.y);
				affinePt.set(pt.x - v1.x, pt.y - v1.y);
			}

			int side = (int) Math.signum(edge.x * affinePt.y - edge.y * affinePt.x);

			if (side == 0)
				return Float.NaN;
			else if (lastSide == 0)
				lastSide = side;
			else if (lastSide != side)
				return Float.NaN;
		}
		
		return t;
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

	public static float raycast(List<BspLeaf> renderedLeaves, Bsp bsp, Vector3f orig, Vector3f dir) {
		float rayLen = Float.POSITIVE_INFINITY;
		for(BspLeaf leaf : renderedLeaves) {
			float dist = leafRay(leaf, bsp, orig, dir);
			if (dist < rayLen) {
				rayLen = dist;
			}
		}
		return rayLen;
	}
	
	public static float leafRay(BspLeaf leaf, Bsp bsp, Vector3f orig, Vector3f dir) {
		float rayLen = Float.POSITIVE_INFINITY;
		int len = leaf.firstFace + leaf.numFaces;
		for(int i = leaf.firstFace; i < len; i++) {
			ArcFace face = bsp.faces[bsp.leafFaceIndices[i]];
			float dist = convexPolygonRay(bsp, face, orig, dir);
			if (dist < rayLen) {
				rayLen = dist;
			}
		}
		
		return rayLen;
	}
}

class Bounds {
	public float max, min;

	public Bounds(float max, float min) {
		this.max = max;
		this.min = min;
	}
}
