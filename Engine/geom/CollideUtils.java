package geom;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import dev.cmd.Console;
import gl.line.LineRender;
import map.architecture.ActiveLeaves;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import util.Colors;
import util.Vectors;

/**
 * @author Jason Basically just a class to handle collisions with geometry that
 *         either consists of groups of existing geometry to have utilities
 *         useful for assisting collision checks. Probably has some stuff in
 *         common to MathUtils, that's probably refactor-worthy
 */
public class CollideUtils {

	final static Vector3f[] axisPtrs = new Vector3f[] { Vectors.POSITIVE_X, Vectors.POSITIVE_Y, Vectors.POSITIVE_Z};

	public static MTV faceCollide(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, ArcFace face,
			Vector3f normal, BoundingBox box) {

		MTV mtv = new MTV();

		final Vector3f[] facePoints = new Vector3f[face.numEdges * 2];
		for (int i = 0; i < face.numEdges; i++) {
			int id = face.firstEdge + i;
			int surfId = surfEdges[id];
			facePoints[i*2] = vertices[surfId > 0 ? edges[surfId].end : edges[-surfId].start];
			facePoints[i*2 + 1] = vertices[surfId > 0 ? edges[surfId].start : edges[-surfId].end];
			
		}
		
		if (box.intersects(facePoints, normal)) {
			mtv.setDepth(box.getIntersectionDepth());
			mtv.setAxis(box.getIntersectionAxis());
			mtv.finish(facePoints[0], face);
			return mtv;
		}
		return null;
	}

	/**
	 * Gets the MTV between an axis aligned bounding box and a face defined within a
	 * architecture's BSP
	 * 
	 * @param - vertices the vertices of the BSP
	 * @param - edges the edges of the BSP
	 * @param - surfEdges the surface edgfes of a BSP
	 * @param - face the face of the BSP
	 * @param - normal the face's normal
	 * @param - box the bounding box to collide with
	 * @return The minimum translation vector of the collision, or null if no
	 *         collision occured
	 */
	public static MTV bspFaceBoxCollide(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, ArcFace face,
			Vector3f normal, BoundingBox box) {

		final Vector3f tl = Vectors.sub(box.getCenter(), box.getHalfSize());
		final Vector3f br = Vectors.add(box.getCenter(), box.getHalfSize());
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
		}

		// Test face normal of face
		float faceOffset = normal.dot(facePoints[0]);
		boxBounds = project(boxPoints, normal);

		if (!mtv.testAxis(boxBounds.max, boxBounds.min, faceOffset, faceOffset, normal))
			return null;

		// Get all edges of face
		Vector3f[] faceEdges = new Vector3f[face.numEdges];
		for (int i = 0; i < faceEdges.length; i++) {
			int id = face.firstEdge + i;

			int surf = surfEdges[id];
			if (surf >= 0) {
				faceEdges[i] = Vectors.sub(vertices[edges[surf].end], vertices[edges[surf].start]);
			} else {
				faceEdges[i] = Vectors.sub(vertices[edges[-surf].start], vertices[edges[-surf].end]);
			}

		}

		for (int i = 0; i < faceEdges.length; i++) {
			for (int j = 0; j < 3; j++) {
				Vector3f axis = faceEdges[i].cross(axisPtrs[j]);
				
				if (Vectors.isZero(axis))
					continue;
				
				boxBounds = project(boxPoints, axis);
				faceBounds = project(facePoints, axis);
				
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
	public static MTV convexHullBoxCollide(Plane[] planes, BoundingBox box) {
		MTV mtv = new MTV();
		
		for (Plane plane : planes) {
			float dist = plane.signedDistanceTo(box.getCenter());
			Vector3f bounds = Vectors.mul(box.getHalfSize(), plane.normal).absolute();
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
	
	public static float raycastMapGeometry(Bsp bsp, ArcFace face, Vector3f rayOrigin, Vector3f rayDir) {
		Vector3f[] vertices = bsp.vertices;
		ArcEdge[] edges = bsp.edges;
		int[] surfEdges = bsp.surfEdges;

		Plane plane = bsp.planes[face.planeId];
		float planeRay = plane.raycast(rayOrigin, rayDir);
		
		if (Float.isNaN(planeRay))
			return Float.NaN;
		
		int e1, e2;
		Vector3f v1, v2;
		Vector3f hit = new Vector3f(rayDir).mul(planeRay).add(rayOrigin);
		
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
			
			Vector3f axis = Vectors.sub(v2, v1).cross(plane.normal);
			float dp = hit.dot(axis);
			
			if (dp > axis.dot(v1)) {
				return Float.NaN;
			}
		}
		
		return planeRay;
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

	public static float raycast(List<BspLeaf> bspLeaves, Bsp bsp, Vector3f orig, Vector3f dir) {
		float rayLen = Float.POSITIVE_INFINITY;
		for(BspLeaf leaf : bspLeaves) {
			float dist = leafRay(leaf, bsp, orig, dir);
			if (dist < rayLen) {
				rayLen = dist;
			}
		}
		return rayLen;
	}
	
	public static float raycast(ActiveLeaves activeLeaves, Bsp bsp, Vector3f orig, Vector3f dir) {
		float rayNear = raycast(activeLeaves.getNear(), bsp, orig, dir);
		if (rayNear <= ActiveLeaves.cutoffDist) {
			return rayNear;
		}
		
		float rayFar = raycast(activeLeaves.getFar(), bsp, orig, dir);
		return Math.min(rayNear, rayFar);
	}
	
	public static float leafRay(BspLeaf leaf, Bsp bsp, Vector3f orig, Vector3f dir) {
		float rayLen = Float.POSITIVE_INFINITY;
		int len = leaf.firstFace + leaf.numFaces;
		for(int i = leaf.firstFace; i < len; i++) {
			ArcFace face = bsp.faces[bsp.leafFaceIndices[i]];
			float dist = raycastMapGeometry(bsp, face, orig, dir);
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
