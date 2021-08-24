package map.architecture.vis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import dev.cmd.Console;
import geom.Plane;
import gl.res.Model;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcTextureMapping;
import map.architecture.util.ArcUtil;

public class BspLeaf {
	
	public short clusterId;
	public Vector3f min, max;
	public short firstFace;
	public short numFaces;
	public short firstAmbientSample;
	public short numAmbientSamples;
	public boolean isUnderwater;

	private Cluster[] clusters;
	public short[] clips;		// A list of IDs to the BSP's clip array
	public int room;			// Pointer to the bsp room array
	public short[] heightmaps;

	public void buildModel(Plane[] planes, ArcEdge[] edges, int[] surfEdges, Vector3f[] vertices, ArcFace[] faces,
			short[] leafFaceIndices, ArcTextureMapping[] texMappings, String[] textureList) {
		// Partition faces by texture
		Map<Integer, List<ArcFace>> faceMap = new HashMap<>();

		int lastFace = firstFace + numFaces;
		for (int j = firstFace; j < lastFace; j++) {
			ArcFace face = faces[leafFaceIndices[j]];
			if (face.texMapping == -1) continue;
			int id = texMappings[face.texMapping].textureId;
			if (id == -1) {
				continue;
			}

			String tex = textureList[id];

			if (tex.equals("INVIS"))
				continue;

			if (faceMap.containsKey(id)) {
				faceMap.get(id).add(face);
			} else {
				List<ArcFace> list = new ArrayList<ArcFace>();
				list.add(face);
				faceMap.put(id, list);
			}
		}

		// Build model for each partition

		clusters = new Cluster[faceMap.keySet().size()];

		int mdlIndex = 0;
		for (int id : faceMap.keySet()) {
			
			List<ArcFace> partitionedFaces = faceMap.get(id);
			int numVerts = 0;
			for (ArcFace face : partitionedFaces) {
				numVerts += face.numEdges - 2;
			}
			numVerts *= 3;

			float[] mdlVerts = new float[numVerts * 3];
			float[] mdlTxtrs = new float[numVerts * 4];
			float[] mdlNorms = new float[numVerts * 3];
			float[] mdlTangents = new float[numVerts * 3];

			int v = 0, t = 0, n = 0, tng = 0;
			
			for (int i = partitionedFaces.size() - 1; i >= 0; i--) {
				ArcFace face = partitionedFaces.get(i);
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

					texVecs = texMappings[face.texMapping].texels;
					lm = texMappings[face.texMapping].lmVecs;
					
					mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
					mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
					ls = ((lm[0][0] * vert.x + lm[0][1] * vert.y + lm[0][2] * vert.z) + lm[0][3] - face.lmMins[0]) / (face.lmSizes[0] + 1);
					lt = ((lm[1][0] * vert.x + lm[1][1] * vert.y + lm[1][2] * vert.z) + lm[1][3] - face.lmMins[1]) / (face.lmSizes[1] + 1);
					ls = (ls * face.lightmapScaleX) + face.lightmapOffsetX;
					lt = (lt * face.lightmapScaleY) + face.lightmapOffsetY;

					mdlTxtrs[t++] = ls;
					mdlTxtrs[t++] = lt;
					
					vert = determineVert(vertices, edges, surfEdges, j);
					mdlVerts[v++] = vert.x;
					mdlVerts[v++] = vert.y;
					mdlVerts[v++] = vert.z;

					mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
					mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
					ls = ((lm[0][0] * vert.x + lm[0][1] * vert.y + lm[0][2] * vert.z) + lm[0][3] - face.lmMins[0]) / (face.lmSizes[0] + 1);
					lt = ((lm[1][0] * vert.x + lm[1][1] * vert.y + lm[1][2] * vert.z) + lm[1][3] - face.lmMins[1]) / (face.lmSizes[1] + 1);
					ls = (ls * face.lightmapScaleX) + face.lightmapOffsetX;
					lt = (lt * face.lightmapScaleY) + face.lightmapOffsetY;
					mdlTxtrs[t++] = ls;
					mdlTxtrs[t++] = lt;
					
					vert = determineVert(vertices, edges, surfEdges, j + 1);
					mdlVerts[v++] = vert.x;
					mdlVerts[v++] = vert.y;
					mdlVerts[v++] = vert.z;
					mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
					mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
					ls = ((lm[0][0] * vert.x + lm[0][1] * vert.y + lm[0][2] * vert.z) + lm[0][3] - face.lmMins[0]) / (face.lmSizes[0] + 1);
					lt = ((lm[1][0] * vert.x + lm[1][1] * vert.y + lm[1][2] * vert.z) + lm[1][3] - face.lmMins[1]) / (face.lmSizes[1] + 1);
					ls = (ls * face.lightmapScaleX) + face.lightmapOffsetX;
					lt = (lt * face.lightmapScaleY) + face.lightmapOffsetY;
					mdlTxtrs[t++] = ls;
					mdlTxtrs[t++] = lt;
				}
			}

			Model model = Model.create();
			model.bind();
			model.createAttribute(0, mdlVerts, 3);
			model.createAttribute(1, mdlTxtrs, 4);
			model.createAttribute(2, mdlNorms, 3);
			model.createAttribute(3, mdlTangents, 3);
			model.unbind();
			
			clusters[mdlIndex] = new Cluster(model, id);
			
			for(int i = 1; i < 3; i++) {
				if (textureList.length - i == id) 
					break;

				char texTypeIdentifier = textureList[id + i].charAt(0);

				if (texTypeIdentifier == '$')
					break;
				if (texTypeIdentifier == '%')
					clusters[mdlIndex].setBumpMapId(id + i);
				else if (texTypeIdentifier == '&')
					clusters[mdlIndex].setSpecMapId(id + i);
			}

			mdlIndex++;
		}
	}

	private Vector3f determineVert(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, int ind) {
		int edgeId = Math.abs(surfEdges[ind]);
		if (surfEdges[ind] < 0) {
			return vertices[edges[edgeId].end];
		}
		return vertices[edges[edgeId].start];
	}

	public void cleanUp() {
		if (clusters == null)
			return;
		for (Cluster cluster : clusters) {
			cluster.getModel().cleanUp();
		}
	}

	public boolean contains(Bsp bsp, Vector3f position) {
		int lastFace = firstFace + numFaces;
		for (int j = firstFace; j < lastFace; j++) {
			if (bsp.planes[bsp.faces[bsp.leafFaceIndices[j]].planeId].classify(position, .001f) == Plane.BEHIND) {
				return false;
			}
		}

		return true;
	}

	public Cluster[] getMeshes() {
		return this.clusters;
	}

	public boolean intersects(Vector3f max2, Vector3f min2) {
		if (min.x > max2.x || max.x < min2.x) return false;
		if (min.y > max2.y || max.y < min2.y) return false;
		if (min.z > max2.z || max.z < min2.z) return false;
	    
	    return true;
	}

}
