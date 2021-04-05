package gl.skybox._3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.map.architecture.render.ArcShader;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Pvs;

public class Skybox3D {
	
	private ArcShader shader;
	private SkyboxCamera skyboxCamera;
	
	public Skybox3D(SkyboxCamera skyboxCamera) {
		this.shader = new ArcShader();
		this.skyboxCamera = skyboxCamera;
	}
	
	public void init(Bsp bsp, Pvs pvs) {
		skyboxCamera.updateLeaf(bsp, pvs);
	}
	
	public void render(Camera camera) {
		
		Matrix4f matrix = new Matrix4f();
		Vector3f position = new Vector3f(skyboxCamera.pos).mul(skyboxCamera.getScale()).negate();
		position.sub(new Vector3f(camera.getPosition()).div(skyboxCamera.getScale()));
		matrix.rotateX(camera.getPitch());
		matrix.rotateY(camera.getYaw());
		matrix.translate(position);
		matrix.scale(skyboxCamera.getScale());
		
		
		Matrix4f tempMatrix = new Matrix4f(camera.getViewMatrix());
		camera.getViewMatrix().set(matrix);
		camera.updateProjection();
		
		shader.start();
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(matrix);
		Resources.getTexture("lightmap").bind(1);
		for(BspLeaf leaf : skyboxCamera.getRenderedLeaves()) {
			for(TexturedModel object : leaf.getMeshes()) {
				
				//if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min)) continue;
				object.getTexture().bind(0);
				object.getModel().bind(0,1,2);
				shader.modelMatrix.loadMatrix(object.getMatrix());
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, object.getModel().getVertexCount());
				Render.drawCalls++;
			}
		}

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
		
		camera.getViewMatrix().set(tempMatrix);camera.updateProjection();
		
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
}
