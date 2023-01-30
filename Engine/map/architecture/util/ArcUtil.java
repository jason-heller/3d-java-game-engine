package map.architecture.util;

import org.joml.Vector3f;

import geom.Plane;
import gl.line.LineRender;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcTextureMapping;
import map.architecture.vis.Bsp;
import util.Vectors;

public class ArcUtil {
	
	public static Vector3f getFaceTangent(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, Plane[] planes, ArcTextureMapping[] texMappings, ArcFace face) {
		Vector3f dPos1 = getEdgeVector(vertices, edges, surfEdges, face.firstEdge);
		Vector3f dPos2 = getEdgeVector(vertices, edges, surfEdges, face.firstEdge + face.numEdges - 1);
		dPos2.negate();
		
		ArcTextureMapping texMap = texMappings[face.texMapping];
		
		// Note: Ignored texels[n][4] since it's irrelevant to the delta
		float[] dTex1 = new float[] {
				(dPos1.x * texMap.texels[0][0] + dPos1.y * texMap.texels[0][1] + dPos1.z * texMap.texels[0][2]),
				(dPos1.x * texMap.texels[1][0] + dPos1.y * texMap.texels[1][1] + dPos1.z * texMap.texels[1][2])
		};
		
		float[] dTex2 = new float[] {
				(dPos2.x * texMap.texels[0][0] + dPos2.y * texMap.texels[0][1] + dPos2.z * texMap.texels[0][2]),
				(dPos2.x * texMap.texels[1][0] + dPos2.y * texMap.texels[1][1] + dPos2.z * texMap.texels[1][2])
		};

		float r = (dTex1[0] * dTex2[1] - dTex1[1] * dTex2[0]);
		Vector3f A = Vectors.mul(dPos1, dTex2[1]);
		Vector3f B = Vectors.mul(dPos2, dTex1[1]);
		Vector3f diff = Vectors.sub(A, B);
		Vector3f tangent = Vectors.div(diff, r).normalize();
		
		//Vector3f normal = planes[face.planeId].normal;
		
		// tangent = (Vectors.sub(tangent, Vector3f.negate(normal).mul(normal.dot(tangent)))).normalize();
		return tangent;
	}
	

	public static Vector3f getVertex(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, int surfEdge, boolean end) {
		ArcEdge edge = edges[Math.abs(surfEdges[surfEdge])];
		boolean getEnd = (end ^ (surfEdge < 0));
		return vertices[getEnd ? edge.end : edge.start];
	}
	
	
	public static Vector3f getEdgeVector(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, int surfEdge) {
		ArcEdge edge = edges[Math.abs(surfEdges[surfEdge])];
		return (surfEdge < 0) ?
				Vectors.sub(vertices[edge.start], vertices[edge.end])
				: Vectors.sub(vertices[edge.end], vertices[edge.start]);
	}

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
			// Plane p = new Plane(p1, Vectors.cross(Vectors.sub(p2, p1), faceNormal));
			// boolean right = p.signedDistanceTo(point) > 0f;
			
			edgeNormal.set(p2).sub(p1);
			edgeNormal.set(edgeNormal.y * faceNormal.z - edgeNormal.z * faceNormal.y,
					edgeNormal.x * faceNormal.z - edgeNormal.z * faceNormal.x,
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
			LineRender.drawLine(Vectors.add(p1, Vectors.POSITIVE_Y), Vectors.add(p2, Vectors.POSITIVE_Y), color);
		}
	}

}
