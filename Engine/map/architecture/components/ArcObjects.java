package map.architecture.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.generic.LightGenericShader;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.ActiveLeaves;
import map.architecture.Architecture;
import map.architecture.vis.BspLeaf;

public class ArcObjects {

	private String[] modelReferences;
	private short[] leafResidenceMap;
	private Map<BspLeaf, List<ArcStaticObject>> objects;
	
	public ArcObjects(BspLeaf[] leaves, String[] objModelReferences, short[] objLeafResidence, List<ArcStaticObject> objList) {
		this.modelReferences = objModelReferences;
		this.leafResidenceMap = objLeafResidence;
		this.objects = new HashMap<>();
		for(ArcStaticObject obj : objList) {
			
			for(int i = 0; i < obj.numObjLeafRes; i++) {
				int leafId = obj.objLeafResIndex + i;
				BspLeaf leaf = leaves[leafResidenceMap[leafId]];
				
				List<ArcStaticObject> batch = this.objects.get(leaf);
				if (batch == null) {
					batch = new ArrayList<>();
					this.objects.put(leaf, batch);
				}
				
				batch.add(obj);
			}
		}
	}

	public String[] getModelReference() {
		return modelReferences;
	}

	public short[] getLeafResidenceMap() {
		return leafResidenceMap;
	}

	public List<ArcStaticObject> getObjects(BspLeaf leaf) {
		return objects.get(leaf);
	}

	public void cleanUp() {
		for(String model : modelReferences) {
			Resources.removeModel(model);
		}
	}

	public void render(Camera camera, Architecture arc, Vector4f clipPlane, Matrix4f lightProjMatrix) {
		LightGenericShader shader = Render.getLightShader();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.clipPlane.loadVec4(clipPlane);
		shader.shadowMap.loadTexUnit(1);
		
		DynamicLight[] lights = arc.getDynamicLightHandler().getLights();
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];
			if (light == null) {
				shader.strength.loadFloat(i, 0f);
				continue;
			}
			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();
			
			Matrix4f lightSpaceMatrix = new Matrix4f();
			Matrix4f.mul(lightProjMatrix	, light.getLightViewMatrix(), lightSpaceMatrix);
			
			shader.lightPos.loadVec3(i, pos.x, pos.y, pos.z);
			shader.lightDir.loadVec3(i, dir.x, dir.y, dir.z);
			shader.cutoff.loadVec2(i, light.getCutoff(), light.getOuterCutoff());
			shader.strength.loadFloat(i, light.getStrength());
			shader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);
			
			Resources.getTexture("shadow" + i).bind(1 + i);
		}

		ActiveLeaves activeLeaves = arc.getActiveLeaves();
		activeLeaves.beginIteration();
		while(activeLeaves.hasNext()) {
			BspLeaf leaf = activeLeaves.next();
			List<ArcStaticObject> batch = objects.get(leaf);
			if (batch == null)
				continue;
			for (ArcStaticObject object : batch) {
				
				Model model = getModel(object.model);
				if (model == null) 
					continue;
				
				shader.lights.loadVec3(arc.getLightsAt(object.lightingPos));

				Matrix4f matrix = new Matrix4f();
				matrix.translate(object.pos);
				matrix.rotate(object.rot);
				shader.modelMatrix.loadMatrix(matrix);
				
				model.bind(0, 1, 2);
				getTexture(object.model).bind(0);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
		}
		
		GL30.glBindVertexArray(0);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		shader.stop();
	}

	public Model getModel(short modelId) {
		if (modelId < 0)
			return null;
		return Resources.getModel(modelReferences[modelId]);
	}

	private Texture getTexture(short modelId) {
		return Resources.getTexture(modelReferences[modelId]);
	}

	public void createBoundingBoxes(List<ArcStaticObject> objList) {
		for(ArcStaticObject obj : objList) {
			obj.setBBox(getModel(obj.model));
		}
	}
}
