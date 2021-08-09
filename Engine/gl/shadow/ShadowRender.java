package gl.shadow;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import core.Application;
import dev.Console;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import map.architecture.vis.BspLeaf;

public class ShadowRender {
	private static final float MAX_SHADOW_RENDER_DIST_SQR = 300 * 300;

	private Matrix4f lightProjection, lightSpaceMatrix;
	
	private ShadowShader shader;
	
	public ShadowRender() {
		//lightProjection = createOrtho(-10, 10, -10, 10, Camera.NEAR_PLANE, Camera.FAR_PLANE);
		lightProjection = createProjView(Camera.fov);
		lightSpaceMatrix = new Matrix4f();
		
		shader = new ShadowShader();
	}
	
	public void render(DynamicLight[] lights, List<BspLeaf> leaves) {
		Render.screen.unbind();
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			
			DynamicLight light = lights[i];
			if (light == null) 
				break;
			
			// If light is too far away, don't render shadows
			Camera camera = Application.scene.getCamera();
			if (Vector3f.distanceSquared(light.getPosition(), camera.getPosition()) > MAX_SHADOW_RENDER_DIST_SQR) {
				continue;
			}
			
			light.updateLightView();
			setProjViewFrustumBounds(0.5f, light.getLightReachInUnits());
			
			Matrix4f.mul(lightProjection, light.getLightViewMatrix(), lightSpaceMatrix);
			
			light.getFbo().bind();
			
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glCullFace(GL11.GL_FRONT);
			
			shader.start();
			shader.lightSpaceMatrix.loadMatrix(lightSpaceMatrix);
			
			for(BspLeaf leaf : leaves) {
				TexturedModel[] tModels = leaf.getMeshes();
				for(TexturedModel tModel : tModels) {
					tModel.getModel().bind(0);
					shader.modelMatrix.loadMatrix(tModel.getMatrix());
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tModel.getModel().getVertexCount());
					Render.drawCalls++;
				}
			}
			
			shader.stop();
			
			GL11.glCullFace(GL11.GL_BACK);
			
			light.getFbo().unbind();
		}
		
		Render.screen.bind();
	}

	private Matrix4f createProjView(int fov) {
		final Matrix4f m = new Matrix4f();
		final float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		final float xScale = (float) (1f / Math.tan(Math.toRadians((fov) / 2f)));
		final float yScale = xScale / aspectRatio;
		// Commented out since this will get replaced by setProjViewFrustumBounds anyways
		// final float frustumLength = Camera.FAR_PLANE - Camera.NEAR_PLANE;

		m.m00 = yScale;
		m.m11 = xScale;
		// m.m22 = -((Camera.FAR_PLANE + Camera.NEAR_PLANE) / frustumLength);
		m.m23 = -1;
		// m.m32 = -(2 * Camera.NEAR_PLANE * Camera.FAR_PLANE / frustumLength);
		m.m33 = 0;
		
		return m;
	}
	
	private void setProjViewFrustumBounds(float near, float far) {
		final float frustumLength = far - near;
		lightProjection.m22 = -((far + near) / frustumLength);
		lightProjection.m32 = -(2 * near * far / frustumLength);
	}
	
	/*private Matrix4f createOrtho(float left, float right, float bottom, float top, float near, float far) {
		Matrix4f m = new Matrix4f();
		m.m00 = 2.0f/(right-left);
		m.m01 = 0.0f;
		m.m02 = 0.0f;
		m.m03 = 0.0f;

		m.m10 = 0.0f;
		m.m11 = 2.0f/(top-bottom);
		m.m12 = 0.0f;
		m.m13 = 0.0f;

		m.m20 = 0.0f;
		m.m21 = 0.0f;
		m.m22 = -2.0f/(far-near);
		m.m23 = 0.0f;

		m.m30 = -(right+left)/(right-left);
		m.m31 = -(top+bottom)/(top-bottom);
		m.m32 =   -(far+near)/(far-near);
		m.m33 = 1.0f;
		
		return m;
	}*/
	
	public void cleanUp() {
		shader.cleanUp();
	}

	public Matrix4f getLightProjectionMatrix() {
		return this.lightProjection;
	}
}
