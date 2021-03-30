package gl.terrain;

import java.util.Collection;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.res.Model;
import map.ground.Chunk;
import map.ground.ChunkMeshHandler;
import map.ground.Terrain;

public class TerrainRender {

	private TerrainShader shader;
	
	public TerrainRender() {
		shader = new TerrainShader();
	}
	
	public void render(Camera camera, Vector3f lightDirection, Terrain terrain) {
		if (Debug.wireframeMode) {
		       GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		       GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		else {
		       GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		       GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDirection);

		Resources.getTexture("grass").bind(0);
		Collection<Chunk> chunkCollection = null;
		
		if (terrain.getPrevChunks().size() > 0) {
			chunkCollection = terrain.getPrevChunks();
		} else {
			chunkCollection = terrain.getChunks();
		}
		
		for(Chunk chunk : chunkCollection) {
			shader.offset.loadVec2(chunk.x, chunk.z);
			Model model = chunk.getModel();
			if (model == null) continue;
			model.bind(0,1,2);
			model.getIndexVbo().bind();
			GL11.glDrawElements(GL11.GL_TRIANGLES, ChunkMeshHandler.indexCount, GL11.GL_UNSIGNED_INT, 0);
			model.unbind(0,1,2);
		}
		shader.stop();
		
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}

}
