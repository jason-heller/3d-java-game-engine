package map.architecture.components;

import java.util.Set;

import org.joml.Vector3f;

import dev.cmd.Console;
import geom.AxisAlignedBBox;
import geom.Plane;
import gl.res.Model;
import util.MathUtil;

/**
 * @author Jason
 *
 */
public class ArcHeightmap {

	public Vector3f origin;
	private AxisAlignedBBox bounds;
	private int firstVertex;
	private int numVertices;
	private int subdivisions;
	private int faceId;
	
	private Vector3f strideX, strideZ;
	
	private short tex1, tex2;
	
	private Model model;
	
	public ArcHeightmap(Vector3f origin, AxisAlignedBBox bounds, int firstVertex, int numVertices, int subdivisions, int faceId, int hmapLightId, short tex1, short tex2) {
		this.origin = origin;
		this.bounds = bounds;
		this.firstVertex = firstVertex;
		this.numVertices = numVertices;
		this.subdivisions = subdivisions;
		this.faceId = faceId;
		this.tex1 = tex1;
		this.tex2 = tex2;
	}

	public void buildModel(ArcHeightmapVertex[] heightmapVerts, ArcFace[] faces, Plane[] planes, ArcEdge[] edges,
			int[] surfEdges, Vector3f[] vertices, ArcTextureMapping[] texData, String[] texRefsArr) {
		ArcFace face = faces[faceId];

		
		float[] verts = new float[numVertices * 3];
		float[] uvs = new float[numVertices * 4];
		float[] blends = new float[numVertices];
		int[] inds = new int[6 * (subdivisions + 1) * (subdivisions + 1)];
		
		float[][] texVecs = texData[face.texMapping].texels;
		float[][] lmVecs = texData[face.texMapping].lmVecs;
		
		int v = 0, u = 0, x = 0;
		
		Plane plane = planes[face.planeId];
		boolean flipNormals = false;
		
		int surf;
		ArcEdge edge;
		
		surf = surfEdges[face.firstEdge + 2];
		edge = edges[Math.abs(surf)];
		strideZ = Vector3f.sub(vertices[edge.end], vertices[edge.start]);
		if (surf > 0)
			strideZ.negate();
		surf = surfEdges[face.firstEdge + 3];
		edge = edges[Math.abs(surf)];
		strideX = Vector3f.sub(vertices[edge.end], vertices[edge.start]);
		if (surf > 0)
			strideX.negate();

		// This is awful
		if (plane.normal.y == 0f) {
			if (plane.normal.z < 0f) {
				flipNormals = true;
				strideZ.mul(-1f);
			}
			
			if (plane.normal.x > 0f) {
				Vector3f temp = Vector3f.negate(strideZ);
				strideZ.set(strideX);
				strideX.set(temp);
			}
		}

		strideX.div(subdivisions);
		strideZ.div(subdivisions);
		
		int vertexCounterX = 0, vertexCounterZ = 0;
		
		Vector3f vert = new Vector3f();
		for(int i = 0; i < numVertices; i++) {
			ArcHeightmapVertex hVert = heightmapVerts[firstVertex + i];
			
			vert.set(origin);
			
			vert.add(Vector3f.mul(strideX, vertexCounterX));
			vert.add(Vector3f.mul(strideZ, vertexCounterZ));
			vertexCounterX++;
			
			if (vertexCounterX > subdivisions) {
				vertexCounterX = 0;
				vertexCounterZ++;
			}
			
			vert.y += hVert.offset;
			
			verts[v++] = vert.x;
			verts[v++] = vert.y;
			verts[v++] = vert.z;
			
			uvs[u++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
			uvs[u++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);


			uvs[u++] = (((lmVecs[0][0] * vert.x + lmVecs[0][1] * vert.y + lmVecs[0][2] * vert.z) + lmVecs[0][3]
					- face.lmMins[0]) / (face.lmSizes[0] + 1) * face.lightmapScaleX) + face.lightmapOffsetX[0];
			float uv = (((lmVecs[1][0] * vert.x + lmVecs[1][1] * vert.y + lmVecs[1][2] * vert.z) + lmVecs[1][3]
					- face.lmMins[1]) / (face.lmSizes[1] + 1) * face.lightmapScaleY);
			uvs[u++] = (face.lightmapOffsetY[0] + face.lightmapScaleY) - uv;

			blends[i] = hVert.getBlend();
		}
		
		int s = subdivisions + 1;
		for(int i = 0; i < subdivisions; i++) {
			for(int j = 0; j < subdivisions; j++) {
				int topLeft = (j * s) + i;
				int topRight = topLeft + 1;
				int btmLeft = ((j + 1) * s) + i;
				int btmRight = btmLeft + 1;
				
				inds[x++] = topLeft;
				
				if (flipNormals) {
					inds[x++] = topRight;
					inds[x++] = btmLeft;
					inds[x++] = btmLeft;
					inds[x++] = topRight;
				} else {
					inds[x++] = btmLeft;
					inds[x++] = topRight;
					inds[x++] = topRight;
					inds[x++] = btmLeft;
				}
				inds[x++] = btmRight;
				
			}
		}
		
		model = Model.create();
		model.bind();
		model.createAttribute(0, verts, 3);
		model.createAttribute(1, uvs, 4);
		model.createAttribute(2, blends, 1);
		model.createIndexBuffer(inds);
		model.unbind();
	}
	
	public boolean intersects(AxisAlignedBBox box) {
		return bounds.intersects(box);
	}
	
	/** gets the height at x, z on the heightmap.
	 * @param x
	 * @param z
	 * @param vertices
	 * @return
	 */
	public float getHeightAt(AxisAlignedBBox bbox, ArcHeightmapVertex[] vertices) {
		float x = bbox.getX();
		float z = bbox.getZ();
		float w = bbox.getWidth();
		float l = bbox.getLength();
		
		float tl = getHeightAt(x+w, z+l, vertices);
		float tr = getHeightAt(x-w, z+l, vertices);
		float bl = getHeightAt(x+w, z-l, vertices);
		float br = getHeightAt(x-w, z-l, vertices);
		
		return Math.max(tl, Math.max(tr, Math.max(bl, br)));
	}
	
	public float getHeightAt(float x, float z, ArcHeightmapVertex[] vertices) {
		Vector3f p1, p2, p3;
		float px = x - origin.x;
		float pz = z - origin.z;
		
		if (px < 0 || pz < 0 || px > bounds.getWidth()*2f || pz > bounds.getLength()*2f) {
			return Float.NEGATIVE_INFINITY;
		}
		
		int xCounter = (int) (px / strideX.x);
		int zCounter = (int) (pz / strideZ.z);
		
		Vector3f left = Vector3f.mul(strideX, xCounter);
		Vector3f right = Vector3f.mul(strideX, xCounter + 1);
		Vector3f bottom = Vector3f.mul(strideZ, zCounter);
		Vector3f top = Vector3f.mul(strideZ, zCounter + 1);		
		int vertexStride = subdivisions + 1;
		int blVertIndex = firstVertex + (xCounter + (zCounter * vertexStride));
		int brVertIndex = blVertIndex + 1;
		int tlVertIndex = blVertIndex + vertexStride;
		int trVertIndex = tlVertIndex + 1;
		
		if (blVertIndex < 0 || trVertIndex > vertices.length) {
			return Float.NEGATIVE_INFINITY;
		}
		
		p1 = Vector3f.add(right, bottom);
		p1.y += (vertices[brVertIndex].offset);
		p2 = Vector3f.add(left, top);
		p2.y += (vertices[tlVertIndex].offset);
		
		if ((p2.x - p1.x) * (pz - p1.z) - (p2.z - p1.z) * (px - p1.x) > 0f) {
			p3 = Vector3f.add(left, bottom);
			p3.y += (vertices[blVertIndex].offset);
		} else {
			p3 = Vector3f.add(right, top);
			p3.y += (vertices[trVertIndex].offset);
		}
		
		return MathUtil.barycentric(px, pz, p1, p2, p3) + origin.y;
	}
	
	public float getBlendAt(float x, float z, ArcHeightmapVertex[] vertices) {
		Vector3f p1, p2, p3;
		float px = x - origin.x;
		float pz = z - origin.z;
		
		if (px < 0 || pz < 0 || px > bounds.getWidth()*2f || pz > bounds.getLength()*2f) {
			return Float.NEGATIVE_INFINITY;
		}
		
		int xCounter = (int) (px / strideX.x);
		int zCounter = (int) (pz / strideZ.z);
		
		Vector3f left = Vector3f.mul(strideX, xCounter);
		Vector3f right = Vector3f.mul(strideX, xCounter + 1);
		Vector3f bottom = Vector3f.mul(strideZ, zCounter);
		Vector3f top = Vector3f.mul(strideZ, zCounter + 1);		
		int vertexStride = subdivisions + 1;
		int blVertIndex = firstVertex + (xCounter + (zCounter * vertexStride));
		int brVertIndex = blVertIndex + 1;
		int tlVertIndex = blVertIndex + vertexStride;
		int trVertIndex = tlVertIndex + 1;
		
		if (blVertIndex < 0 || trVertIndex > vertices.length) {
			return Float.NEGATIVE_INFINITY;
		}
		
		p1 = Vector3f.add(right, bottom);
		p1.y = (vertices[brVertIndex].getBlend());
		p2 = Vector3f.add(left, top);
		p2.y = (vertices[tlVertIndex].getBlend());
		
		if ((p2.x - p1.x) * (pz - p1.z) - (p2.z - p1.z) * (px - p1.x) > 0f) {
			p3 = Vector3f.add(left, bottom);
			p3.y = (vertices[blVertIndex].getBlend());
		} else {
			p3 = Vector3f.add(right, top);
			p3.y = (vertices[trVertIndex].getBlend());
		}
		
		return MathUtil.barycentric(px, pz, p1, p2, p3);
	}

	public Model getModel() {
		return model;
	}
	
	public void cleanUp() {
		model.cleanUp();
	}

	public int getFaceId() {
		return faceId;
	}
	
	public int getTexture1() {
		return tex1;
	}
	
	public int getTexture2() {
		return tex2;
	}
	
}