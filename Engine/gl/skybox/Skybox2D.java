package gl.skybox;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;

public class Skybox2D implements Skybox {
	
	private SkyboxShader shader;
	private Model box;

	
	public Skybox2D() {
		this.shader = new SkyboxShader();
		createSkyboxModel();
	}
	
	private void createSkyboxModel() {
		box = Model.create();
		box.bind();
		box.createIndexBuffer(INDICES);
		box.createAttribute(0, getVertexPositions(3000f), 3);
		box.unbind();
	}
	
	private static final int[] INDICES = { 0, 1, 3, 1, 2, 3, 1, 5, 2, 2, 5, 6, 4, 7, 5, 5, 7, 6, 0,
			3, 4, 4, 3, 7, 7, 3, 6, 6, 3, 2, 4, 5, 0, 0, 5, 1 };
	
	private static float[] getVertexPositions(float size) {
		return new float[] { -size, size, size, size, size, size, size, -size, size, -size, -size,
				size, -size, size, -size, size, size, -size, size, -size, -size, -size, -size,
				-size };
	}
	
	public void render(Architecture arc, Camera camera) {
		shader.start();
		Matrix4f matrix = new Matrix4f(camera.getViewMatrix());
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		
		shader.projectionViewMatrix.loadMatrix(Matrix4f.mul(camera.getProjectionMatrix(), matrix, null));
		Texture t = Resources.getTexture("skybox");
		if (t!=null) {
			t.bind(0);
		}
		
		box.bind(0);
		GL11.glDrawElements(GL11.GL_TRIANGLES, box.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		Render.drawCalls++;
		box.unbind(0);
		shader.stop();
		
		
	}
	
	public void cleanUp() {
		box.cleanUp();
		shader.cleanUp();
	}
}
