package gl.entity;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.arc.ArcRenderMaster;
import gl.generic.LightGenericShader;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.vis.BspLeaf;
import scene.entity.Entity;

public class EntityRender {

	public static Model billboard;
	
	public EntityRender() {
		billboard = createBillBoardedModel();
	}
	
	private Model createBillBoardedModel() {
		Model model = Model.create();
		model.bind(0, 1, 2);
		model.createAttribute(0, new float[] { -1f, -1f, 0f, 1f, -1f, 0f, 1f, 1f, 0f, -1f, 1f, 0f }, 3);
		model.createAttribute(1, new float[] { 1, 1, 0, 1, 0, 0, 1, 0 }, 2);
		model.createAttribute(2, new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, 3);
		model.createIndexBuffer(new int[] { 0, 1, 3, 3, 1, 2 });
		model.unbind(0, 1, 2);
		return model;
	}
	
	public void render(Camera camera, Architecture arc, Vector4f clipPlane, Map<BspLeaf, List<Entity>> entities) {
		LightGenericShader shader = Render.getLightShader();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.clipPlane.loadVec4(clipPlane);
		shader.shadowMap.loadTexUnit(1);
		
		// Arc stuff
		DynamicLight[] lights = arc.getDynamicLightHandler().getLights();
		Matrix4f lightProjectionMatrix = ArcRenderMaster.getShadowProjectionMatrix();
		//
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];
			if (light == null) {
				shader.strength.loadFloat(i, 0f);
				continue;
			}
			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();
			
			Matrix4f lightSpaceMatrix = new Matrix4f();
			Matrix4f.mul(lightProjectionMatrix, light.getLightViewMatrix(), lightSpaceMatrix);
			
			shader.lightPos.loadVec3(i, pos.x, pos.y, pos.z);
			shader.lightDir.loadVec3(i, dir.x, dir.y, dir.z);
			shader.cutoff.loadVec2(i, light.getCutoff(), light.getOuterCutoff());
			shader.strength.loadFloat(i, light.getStrength());
			shader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);
			
			Resources.getTexture("shadow" + i).bind(1 + i);
		}
		
		if (Debug.ambientOnly) {
			Resources.getTexture("none").bind(0);
		}
		
		for(BspLeaf leaf : arc.getRenderedLeaves()) {
			List<Entity> batch = entities.get(leaf);
			if (batch == null) continue;
			
			for(Entity entity : batch) {
				render(arc, camera, entity, shader);
			}
		}
		
		GL30.glBindVertexArray(0);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		shader.stop();
	}
	
	private void render(Architecture arc, Camera camera, Entity entity, LightGenericShader shader) {
		Model model = entity.getModel();
		
		if (model == null || !entity.visible) 
			return;
		
		Texture texture = entity.getTexture();
		
		model.getMeshData().update(model, texture, entity.getMatrix());
		
		if (!Debug.ambientOnly) {
			if (texture == null)
				Resources.getTexture("default").bind(0);
			else
				texture.bind(0);
		}
		
		shader.lights.loadVec3(arc.getLightsAt(entity.pos));
		
		if (model == billboard) {
			Matrix4f matrix = new Matrix4f();
			matrix.translate(entity.pos);
			matrix.rotateY(-camera.getYaw());
			matrix.scale(entity.scale);
			matrix.scale(texture.width / 128f, texture.height / 128f, 1f);
			shader.modelMatrix.loadMatrix(matrix);
		} else {
			shader.modelMatrix.loadMatrix(entity.getMatrix());
		}
		
		model.bind(0, 1, 2);
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
	}

	public void cleanUp() {
		billboard.cleanUp();
	}
}
