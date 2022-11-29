package gl.arc.decal;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.fbo.FBO;
import gl.res.Model;

public class DynamicDecalRender {
	private static DynamicDecalShader shader;
	private static Model CUBE;
	
	public static void init() {
		shader = new DynamicDecalShader();
		CUBE = Resources.getModel("cube");
	}
	
	public static void render(Camera camera, FBO fbo, List<DynamicDecal> dynamicDecals) {
		shader.start();
		
		Matrix4f viewModel = new Matrix4f();
		Matrix4f invViewModel = new Matrix4f();

		Matrix4f invProjection = new Matrix4f();
		Matrix4f.invert(camera.getProjectionMatrix(), invProjection);
		shader.projection.loadMatrix(camera.getProjectionMatrix());
		shader.invProj.loadMatrix(invProjection);
		
		shader.albedo.loadTexUnit(0);
		shader.depthSamples.loadTexUnit(1);
		
		//fbo.bindColorBuffer(0);
		Render.screen.bindDepthBuffer(1);
		
		CUBE.bind(0);
		for(DynamicDecal decal : dynamicDecals) {

			Matrix4f.mul(camera.getViewMatrix(), decal.getModelMatrix(), viewModel);
			Matrix4f.invert(viewModel, invViewModel);
			shader.viewModel.loadMatrix(viewModel);
			shader.invViewModel.loadMatrix(invViewModel);
			
			decal.getTexture().bind(1);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, CUBE.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		CUBE.unbind(0);
		shader.stop();
	}
	
	public static void cleanUp() {
		shader.cleanUp();
	}
}
