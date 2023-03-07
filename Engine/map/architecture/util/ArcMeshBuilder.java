package map.architecture.util;

import org.joml.Vector3f;

import geom.Plane;
import gl.res.Mesh;
import gl.res.Model;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcTextureMapping;
import map.architecture.vis.Bsp;

public class ArcMeshBuilder {
	
	public static Model buildModel(Bsp bsp, int firstFace, int lastFace) {
		
		final Vector3f[] vertices = bsp.vertices;
		final ArcEdge[] edges = bsp.edges;
		final int[] surfEdges = bsp.surfEdges;
		final Plane[] planes = bsp.planes;
		final ArcTextureMapping[] texMappings = bsp.getTextureMappings();
		
		int numVerts = 0;
		
		for (int i = firstFace; i < lastFace; i++) {
			final ArcFace face = bsp.faces[i];
			numVerts += face.numEdges - 2;
		}
		
		numVerts *= 3;
		
		float[] mdlVerts = new float[numVerts * 3];
		float[] mdlTxtrs = new float[numVerts * 4];
		float[] mdlNorms = new float[numVerts * 3];
		float[] mdlTangents = new float[numVerts * 3];
		int[] mdlIndices = new int[numVerts * 3];

		int v = 0, t = 0, n = 0, i = 0, tng = 0;
		
		for (int x = firstFace; x < lastFace; x++) {
			final ArcFace face = bsp.faces[x];
			
			// Fullbright maps might have the offsets be null
			float lmOffX = face.lightmapOffsetX == null ? 0 : face.lightmapOffsetX[0];
			float lmOffY = face.lightmapOffsetY == null ? 0 : face.lightmapOffsetY[0];
			
			int lastEdge = face.firstEdge + face.numEdges;

			Vector3f tangent = ArcUtil.getFaceTangent(vertices, edges, surfEdges, planes, texMappings, face);
			
			for (int j = face.firstEdge + 1; j < lastEdge - 1; j++) {
				// Gross but fast
				Vector3f vert;
				float[][] texVecs;
				float[][] lm;
				float ls, lt;
				Vector3f norm = planes[face.planeId].normal;
				for(int k = 0; k < 3; k++) {
					mdlNorms[n++] = norm.x;
					mdlNorms[n++] = norm.y;
					mdlNorms[n++] = norm.z;
					mdlTangents[tng++] = tangent.x;
					mdlTangents[tng++] = tangent.y;
					mdlTangents[tng++] = tangent.z;
				}

				vert = determineVert(vertices, edges, surfEdges, face.firstEdge);
				mdlVerts[v++] = vert.x;
				mdlVerts[v++] = vert.y;
				mdlVerts[v++] = vert.z;
				mdlIndices[i] = i++;
				mdlIndices[i] = i++;
				mdlIndices[i] = i++;

				texVecs = texMappings[face.texMapping].texels;
				lm = texMappings[face.texMapping].lmVecs;
				
				mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
				mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
				ls = ((lm[0][0] * vert.x + lm[0][1] * vert.y + lm[0][2] * vert.z) + lm[0][3] - face.lmMins[0]) / (face.lmSizes[0] + 1);
				lt = ((lm[1][0] * vert.x + lm[1][1] * vert.y + lm[1][2] * vert.z) + lm[1][3] - face.lmMins[1]) / (face.lmSizes[1] + 1);
				ls = (ls * face.lightmapScaleX) + lmOffX;
				lt = (lt * face.lightmapScaleY) + lmOffY;

				mdlTxtrs[t++] = ls;
				mdlTxtrs[t++] = lt;
				
				vert = determineVert(vertices, edges, surfEdges, j);
				mdlVerts[v++] = vert.x;
				mdlVerts[v++] = vert.y;
				mdlVerts[v++] = vert.z;
				mdlIndices[i] = i++;
				mdlIndices[i] = i++;
				mdlIndices[i] = i++;

				mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
				mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
				ls = ((lm[0][0] * vert.x + lm[0][1] * vert.y + lm[0][2] * vert.z) + lm[0][3] - face.lmMins[0]) / (face.lmSizes[0] + 1);
				lt = ((lm[1][0] * vert.x + lm[1][1] * vert.y + lm[1][2] * vert.z) + lm[1][3] - face.lmMins[1]) / (face.lmSizes[1] + 1);
				ls = (ls * face.lightmapScaleX) + lmOffX;
				lt = (lt * face.lightmapScaleY) + lmOffY;
				mdlTxtrs[t++] = ls;
				mdlTxtrs[t++] = lt;
				
				vert = determineVert(vertices, edges, surfEdges, j + 1);
				mdlVerts[v++] = vert.x;
				mdlVerts[v++] = vert.y;
				mdlVerts[v++] = vert.z;
				mdlIndices[i] = i++;
				mdlIndices[i] = i++;
				mdlIndices[i] = i++;
				mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
				mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
				ls = ((lm[0][0] * vert.x + lm[0][1] * vert.y + lm[0][2] * vert.z) + lm[0][3] - face.lmMins[0]) / (face.lmSizes[0] + 1);
				lt = ((lm[1][0] * vert.x + lm[1][1] * vert.y + lm[1][2] * vert.z) + lm[1][3] - face.lmMins[1]) / (face.lmSizes[1] + 1);
				ls = (ls * face.lightmapScaleX) + lmOffX;
				lt = (lt * face.lightmapScaleY) + lmOffY;
				mdlTxtrs[t++] = ls;
				mdlTxtrs[t++] = lt;
			}
		}
		
		// This will only allow one texture, since we wrapped everything into one mesh. Can extend later
		Mesh mesh = Mesh.create();
		mesh.bind();
		mesh.createAttribute(0, mdlVerts, 3);
		mesh.createAttribute(1, mdlTxtrs, 4);
		mesh.createAttribute(2, mdlNorms, 3);
		mesh.createAttribute(3, mdlTangents, 3);
		mesh.createIndexBuffer(mdlIndices);
		mesh.unbind();
		
		Model model = new Model(1);
		model.setMesh(0, mesh);
		
		return model;
	}	
	
	private static Vector3f determineVert(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, int ind) {
		int edgeId = Math.abs(surfEdges[ind]);
		if (surfEdges[ind] < 0) {
			return vertices[edges[edgeId].end];
		}
		return vertices[edges[edgeId].start];
	}
}
