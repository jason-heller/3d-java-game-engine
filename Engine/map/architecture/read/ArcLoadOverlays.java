package map.architecture.read;

import java.io.IOException;

import org.joml.Vector2f;
import org.joml.Vector3f;

import core.Resources;
import gl.res.Mesh;
import gl.res.Texture;
import io.FileUtils;
import map.architecture.components.ArcTextureData;
import map.architecture.components.ArcTextureMapping;
import map.architecture.vis.Bsp;
import scene.entity.EntityHandler;
import scene.entity.object.map.OverlayEntity;
import util.CounterInputStream;

public class ArcLoadOverlays {

	private static final int FACES_BITMASK = 16383;
	//private static final int ORDER_BITMASK = 49152;

	static void readOverlays(Bsp bsp, ArcTextureData texData, CounterInputStream in) throws IOException {
		final int numOverlays = in.readShort();

		for (int i = 0; i < numOverlays; i++) {
			short textureId = in.readShort();
			int numFacesAndOrder = in.readUnsignedShort();
			int leafId = in.readShort();
			int numFaces = numFacesAndOrder & FACES_BITMASK;
			//int order = numFacesAndOrder & ORDER_BITMASK;
			
			int[] faces = new int[numFaces];
			for(int j = 0; j < numFaces; j++) {
				faces[j] = in.readShort();
			}
			
			Vector2f[] texCoords = new Vector2f[] { 
					new Vector2f(in.readFloat(), in.readFloat()),
					new Vector2f(in.readFloat(), in.readFloat())
			};

			Vector3f[] points = new Vector3f[] { 
					FileUtils.readVec3(in), FileUtils.readVec3(in), FileUtils.readVec3(in), FileUtils.readVec3(in)
			};
			
			Vector3f origin = FileUtils.readVec3(in);
			origin.y += .2f;
			Vector3f normal = FileUtils.readVec3(in);
			
			textureId = 0;
			//
			ArcTextureMapping mapping = bsp.getTextureMappings()[textureId];
			Texture texture = texData.getTextures()[mapping.textureId];
			texture = Resources.NO_TEXTURE;
			
			Mesh mesh = createOverlayModel(numFaces, texCoords, points, origin, normal);
			OverlayEntity overlay = new OverlayEntity(origin, mesh, texture, bsp.leaves[leafId]);
			EntityHandler.addEntity(overlay);
		}
		
	}

	private static Mesh createOverlayModel(int numFaces, Vector2f[] texCoords, Vector3f[] points,
			Vector3f origin, Vector3f normal) {
		final int numVertices = 4 * numFaces;
		float[] vertices = new float[numVertices * 3];
		float[] uvs = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		int[] indices = new int[numFaces * 6];
		
		final float[] texOrder = new float[] {
			texCoords[0].x, texCoords[0].y,	
			texCoords[1].x, texCoords[0].y,	
			texCoords[0].x, texCoords[1].y,	
			texCoords[1].x, texCoords[1].y
		};
		
		int v = 0, u = 0, n = 0, i = 0;
		for(int x = 0; x < numFaces; x++) {
			
			for(int y = 0; y < 4; y++) {
				vertices[v++] = (points[y].x);
				vertices[v++] = (points[y].y);
				vertices[v++] = (points[y].z);
				
				uvs[u++] = texOrder[y * 2];
				uvs[u++] = texOrder[y * 2 + 1];
				
				normals[n++] = normal.x;
				normals[n++] = normal.y;
				normals[n++] = normal.z;
			}
			
			int z = x * 4;
			indices[i++] = z;
			indices[i++] = z + 1;
			indices[i++] = z + 3;
			indices[i++] = z + 3;
			indices[i++] = z + 1;
			indices[i++] = z + 2;
		}
		
		Mesh mesh = Mesh.create();
		mesh.bind();
		mesh.createAttribute(0, vertices, 3);
		mesh.createAttribute(1, uvs, 2);
		mesh.createAttribute(2, normals, 3);
		mesh.createIndexBuffer(indices);
		mesh.unbind();
		return mesh;
	}
}
