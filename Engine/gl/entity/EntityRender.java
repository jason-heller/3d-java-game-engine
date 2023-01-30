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
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.ActiveLeaves;
import map.architecture.Architecture;
import map.architecture.vis.BspLeaf;
import scene.entity.Entity;

public class EntityRender {

	public static Mesh billboard;
	
	public EntityRender() {
		billboard = createBillBoardedModel();
	}
	
	private Mesh createBillBoardedModel() {
		Mesh model = Mesh.create();
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
			lightProjectionMatrix.mul(light.getLightViewMatrix(), lightSpaceMatrix);
			
			shader.lightPos.loadVec3(i, pos.x, pos.y, pos.z);
			shader.lightDir.loadVec3(i, dir.x, dir.y, dir.z);
			shader.cutoff.loadVec2(i, light.getCutoff(), light.getOuterCutoff());
			shader.strength.loadFloat(i, light.getStrength());
			shader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);
			
			Resources.getTexture("shadow" + i).bind(1 + i);
		}
		
		if (Debug.ambientOnly) {
			Resources.NO_TEXTURE.bind(0);
		}
		
		ActiveLeaves activeLeaves = arc.getActiveLeaves();
		activeLeaves.beginIteration();
		while(activeLeaves.hasNext()) {
			BspLeaf leaf = activeLeaves.next();
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
		
		if (model == null || !entity.visible || entity.getAnimator() != null) 
			return;

		int numMeshes = model.getMeshes().length;
		for(int i = 0; i < numMeshes; i++) {
			Mesh mesh = model.getMeshes()[i];
			Texture texture = model.getTextures()[i];

			mesh.getMeshData().update(mesh, texture, entity.getMatrix());

			if (!Debug.ambientOnly) {
				if (texture == null)
					Resources.DEFAULT.bind(0);
				else
					texture.bind(0);
			}
			
			shader.lights.loadVec3(arc.getLightsAt(entity.position));
			Vector3f color = entity.getColor();
			shader.color.loadVec4(color.x, color.y, color.z, entity.getColorBlendFactor());
			
			if (mesh == billboard) {
				Matrix4f matrix = new Matrix4f();
				matrix.translate(entity.position);
				matrix.rotateY(-camera.getYaw());
				matrix.scale(entity.scale.x, entity.scale.y, entity.scale.z);
				matrix.scale(texture.width / 128f, texture.height / 128f, 1f);
				shader.modelMatrix.loadMatrix(matrix);
			} else {
				shader.modelMatrix.loadMatrix(entity.getMatrix());
			}

			mesh.bind(0, 1, 2);
			GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
	}

	public void cleanUp() {
		billboard.cleanUp();
	}
}
