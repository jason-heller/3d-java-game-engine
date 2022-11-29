package gl.water;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import gl.Camera;
import gl.Render;

public class WaterRender {

	private static WaterShader waterShader;
	
	public static void init() {
		waterShader = new WaterShader();
	}
	
	public static void renderWater(Camera camera, Vector3f max, Vector3f min) {

		waterShader.start();
		waterShader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		
		waterShader.setup(camera);

		GL11.glEnable(GL11.GL_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		Vector3f bounds = Vector3f.sub(max, min);
		waterShader.offset.loadVec3(min.x, max.y, min.z);
		waterShader.scales.loadVec2(bounds.x, bounds.z);
		waterShader.timer.loadFloat(Render.getTimer());
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, 12, GL11.GL_UNSIGNED_INT, 0);
		Render.drawCalls++;

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		
		GL11.glDisable(GL11.GL_ALPHA);
		waterShader.stop();
	}
	
	public static void cleanUp() {
		waterShader.cleanUp();
	}
}
