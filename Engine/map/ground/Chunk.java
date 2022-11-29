package map.ground;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import gl.res.Mesh;
import gl.res.Vbo;

public class Chunk {
	
	private Mesh model;
	private Vbo heightNormalVbo;
	private boolean loaded;
	public int x, z;
	private int tesselation;
	
	public Chunk(int x, int z, int tesselation) {
		this.x = x;
		this.z = z;
		this.tesselation = tesselation;
		model = null;
	}

	public void addHeights(Vector4f[][] heights) {
		model = Mesh.create();
		Vbo vertexVbo = ChunkMeshHandler.vertexVbos[tesselation];
		Vbo texCoordVbo = ChunkMeshHandler.textureCoordVbo;
		Vbo indexVbo = ChunkMeshHandler.indexVbo;
		heightNormalVbo = ChunkMeshHandler.buildHeightNormalVbo(x, z, tesselation, heights);
		model.bind();
		vertexVbo.bind();
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0);
		vertexVbo.unbind();
		texCoordVbo.bind();
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 2 * 4, 0);
		texCoordVbo.unbind();
		heightNormalVbo.bind();
		GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 4 * 4, 0);
		heightNormalVbo.unbind();
		model.setIndexBuffer(indexVbo, ChunkMeshHandler.indexCount);
		
		model.unbind();
	}
	
	public Mesh getModel() {
		return model;
	}
	
	public boolean isLoaded() {
		return loaded;
	}

	public void cleanUp() {
		// DO NOT DELETE MODEL HERE
		if (model == null) return;
		heightNormalVbo.delete();
		GL30.glDeleteVertexArrays(model.id);
	}

	public int getTesselation() {
		return tesselation;
	}
}
