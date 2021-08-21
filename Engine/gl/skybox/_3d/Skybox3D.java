package gl.skybox._3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.arc.ArcShader;
import gl.skybox.Skybox;
import gl.skybox.Skybox2D;
import map.architecture.Architecture;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Cluster;
import map.architecture.vis.Pvs;

public class Skybox3D implements Skybox {
	
	private ArcShader shader;
	private SkyboxCamera skyboxCamera;
	
	private Skybox2D skybox2D;
	
	public Skybox3D(SkyboxCamera skyboxCamera, Bsp bsp, Pvs pvs) {
		this.shader = new ArcShader();
		this.skyboxCamera = skyboxCamera;
		skyboxCamera.updateLeaf(bsp, pvs);
		
		skybox2D = new Skybox2D();
	}
	
	public void render(Architecture arc, Camera camera) {
		skybox2D.render(arc, camera);
		
		
		Matrix4f matrix = new Matrix4f();
		Vector3f position = new Vector3f(skyboxCamera.pos).mul(skyboxCamera.getScale()).negate();
		position.sub(new Vector3f(camera.getPosition()).div(skyboxCamera.getScale()));
		matrix.rotateX(camera.getEffectedPitch());
		matrix.rotateY(camera.getEffectedYaw());
		matrix.rotateZ(camera.getEffectedRoll());
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

		//shader.sampler.loadTexUnit(0);
		shader.lightmap.loadTexUnit(1);
		Resources.getTexture(Debug.fullbright ? "flat" : "lightmap").bind(1);
		for(BspLeaf leaf : skyboxCamera.getRenderedLeaves()) {
			for(Cluster cluster : leaf.getMeshes()) {
				
				arc.getTextures()[cluster.getDiffuseId()].bind(0);
				cluster.getModel().bind(0,1,2);
				// shader.modelMatrix.loadMatrix(object.getMatrix());
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cluster.getModel().getVertexCount());
				Render.drawCalls++;
			}
		}

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
		
		camera.getViewMatrix().set(tempMatrix);
		camera.updateProjection();
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
	}
	
	public void cleanUp() {
		skybox2D.cleanUp();
		shader.cleanUp();
	}
}
