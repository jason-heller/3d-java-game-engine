package gl.map.architecture.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Debug;
import gl.TexturedModel;

public class ArcRender {
	
	private static ArcShader shader;
	
	public static void init() {
		shader = new ArcShader();
	}
	
	public static void render(Matrix4f projection, Matrix4f view, TexturedModel tMesh) {
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		shader.start();
		shader.projectionMatrix.loadMatrix(projection);
		shader.viewMatrix.loadMatrix(view);
		//shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		shader.camPos.loadVec3(view.getTranslation());
		
		shader.sampler.loadTexUnit(0);
		shader.lightmap.loadTexUnit(1);
		(Debug.ambientOnly ? Resources.getTexture("none") : tMesh.getTexture()).bind(0);
		Resources.getTexture(Debug.fullbright ? "none" : "lightmap").bind(1);

		tMesh.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(tMesh.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tMesh.getModel().getVertexCount());

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
		
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public static void cleanUp() {
		shader.cleanUp();
	}
}
