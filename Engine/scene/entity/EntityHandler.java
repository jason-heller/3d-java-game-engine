package scene.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.entity.GenericShader;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
import scene.PlayableScene;

public class EntityHandler {
	private static Map<Texture, List<Entity>> entities = new HashMap<>();
	public static Model billboard;
	
	public EntityHandler() {
		entities = new HashMap<>();
		billboard = createBillBoardedModel();
	}
	
	private Model createBillBoardedModel() {
		Model model = Model.create();
		model.bind(0, 1, 2);
		model.createAttribute(0, new float[] {-1f, -1f, 0f, 1f, -1f, 0f, 1f, 1f, 0f, -1f, 1f, 0f}, 3);
		model.createAttribute(1, new float[] {1,1, 0,1, 0,0, 1,0}, 2);
		model.createAttribute(2, new float[] {1,1,1, 1,1,1, 1,1,1, 1,1,1}, 3);
		model.createIndexBuffer(new int[] {0,1,3, 3,1,2});
		model.unbind(0, 1, 2);
		return model;
	}

	public static void addEntity(Entity entity) {
		Texture texture = entity.getTexture();
		List<Entity> batch = entities.get(texture);
		
		if (batch == null) {
			batch = new ArrayList<Entity>();
			entities.put(texture, batch);
		}
		
		batch.add(entity);
	}
	
	public static void removeEntity(Entity entity) {
		Texture texture = entity.getTexture();
		entities.get(texture).remove(entity);
	}
	
	public void update(PlayableScene scene) {
		for(List<Entity> batch : entities.values()) {
			for(Entity entity : batch) {
				entity.update(scene);
			}
		}
	}
	
	public void render(Camera camera, Architecture arc) {
		GenericShader shader = Render.getGenericShader();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		for(Texture texture : entities.keySet()) {
			
			if (texture == null)
				Resources.getTexture("default").bind(0);
			else
				texture.bind(0);
			
			List<Entity> batch = entities.get(texture);
			for(Entity entity : batch) {
				Model model = entity.getModel();
				if (model == null) 
					continue;
				
				
				shader.lightDirection.loadVec3(entity.lighting);
				
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
		}
		
		GL30.glBindVertexArray(0);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		shader.stop();
	}
	
	public void cleanUp() {
		for(List<Entity> batch : entities.values()) {
			for(Entity entity : batch) {
				entity.cleanUp();
			}
		}
		entities.clear();
		billboard.cleanUp();
	}
}
