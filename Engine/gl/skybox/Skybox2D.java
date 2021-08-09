package gl.skybox;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.res.Model;
import gl.res.Texture;

public class Skybox2D {
	
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
	
	public void render(Camera camera, Vector3f sunDirection) {
		shader.start();
		Matrix4f matrix = new Matrix4f(camera.getViewMatrix());
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		//matrix.rotateX(camera.getPitch());
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(matrix);
		shader.lightDir.loadVec3(sunDirection);
		
		//GL11.glDisable(GL11.GL_BLEND);
		//GL11.glCullFace(GL11.GL_BACK);
		//GL11.glDisable(GL13.GL_MULTISAMPLE);
		
		//SkyboxController.getTexture().bind(0);
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
