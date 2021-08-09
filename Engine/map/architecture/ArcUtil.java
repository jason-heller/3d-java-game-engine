package map.architecture;

import org.joml.Vector3f;

import gl.line.LineRender;
import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;

public class ArcUtil {

	public static boolean faceContainsPoint(Bsp bsp, ArcFace face, Vector3f point) {
		final int edgeStart = face.firstEdge;
		final int edgeEnd = edgeStart + face.numEdges;
		
		Vector3f faceNormal = bsp.planes[face.planeId].normal;
		Vector3f edgeNormal = new Vector3f();
		float edgeDist = 0f;
		
		int side = 0;
		
		for(int j = edgeStart; j < edgeEnd; j++) {
			Vector3f p1, p2;
			int edgeId = bsp.surfEdges[j];
			
			if (edgeId < 0) {
				p1 = bsp.vertices[bsp.edges[-edgeId].end];
				p2 = bsp.vertices[bsp.edges[-edgeId].start];
			} else {
				p1 = bsp.vertices[bsp.edges[edgeId].start];
				p2 = bsp.vertices[bsp.edges[edgeId].end];
			}
			
			// Same as below, not sure if this optimization does anything for us
			// Plane p = new Plane(p1, Vector3f.cross(Vector3f.sub(p2, p1), faceNormal));
			// boolean right = p.signedDistanceTo(point) > 0f;
			
			edgeNormal.set(p2).sub(p1);
			edgeNormal.set(edgeNormal.y * faceNormal.z - edgeNormal.z * faceNormal.y,
					edgeNormal.x * faceNormal.z - edgeNormal.z * faceNormal.y,
					edgeNormal.x * faceNormal.y - edgeNormal.y * faceNormal.x);
			edgeDist = (edgeNormal.x * p1.x + edgeNormal.y * p1.y + edgeNormal.z * p1.z);

			boolean right = point.dot(edgeNormal) - edgeDist > 0f;

			if (side == 0) {
				side = right ? 1 : -1;
			} else if (right && side == -1) {
				return false;
			} else if (!right && side == 1) {
				return false;
			}
		}
		
		return true;
	}

	public static void drawFaceHighlight(Bsp bsp, ArcFace face, Vector3f color) {
		for(int i = face.firstEdge; i < face.firstEdge + face.numEdges; i++) {
			int surfEdge = Math.abs(bsp.surfEdges[i]);
			Vector3f p1 = bsp.vertices[bsp.edges[surfEdge].start];
			Vector3f p2 = bsp.vertices[bsp.edges[surfEdge].end];
			LineRender.drawLine(Vector3f.add(p1, Vector3f.Y_AXIS), Vector3f.add(p2, Vector3f.Y_AXIS), color);
		}
	}

}
