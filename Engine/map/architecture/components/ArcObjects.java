package map.architecture.components;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Console;
import gl.Camera;
import gl.Render;
import gl.generic.GenericShader;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
import scene.entity.Entity;

public class ArcObjects {

	private String[] modelReferences;
	private short[] leafResidenceMap;
	private ArcStaticObject[] objects;
	
	public ArcObjects(String[] objModelReferences, short[] objLeafResidence, ArcStaticObject[] objects) {
		this.modelReferences = objModelReferences;
		this.leafResidenceMap = objLeafResidence;
		this.objects = objects;
	}

	public String[] getModelReference() {
		return modelReferences;
	}

	public short[] getLeafResidenceMap() {
		return leafResidenceMap;
	}

	public ArcStaticObject[] getObjects() {
		return objects;
	}

	public void cleanUp() {
		for(String model : modelReferences) {
			Resources.removeModel(model);
		}
	}

	public void render(Camera camera, Architecture arc) {
		GenericShader shader = Render.getGenericShader();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		
		for(ArcStaticObject object : objects) {
			
			Model model = getModel(object.model);
			if (model == null) 
				continue;
			
			shader.lights.loadVec3(arc.getLightsAt(object.lightingPos));
			
			Matrix4f matrix = new Matrix4f();
			matrix.translate(object.pos);
			matrix.rotate(object.rot);
			shader.modelMatrix.loadMatrix(matrix);
			
			model.bind(0, 1, 2);
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		
		GL30.glBindVertexArray(0);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		shader.stop();
	}

	private Model getModel(short modelId) {
		if (modelId < 0)
			return null;
		return Resources.getModel(modelReferences[modelId]);
	}
}
