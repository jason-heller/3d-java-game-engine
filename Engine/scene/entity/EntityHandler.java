package scene.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Console;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.generic.GenericShader;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;

public class EntityHandler {

	private static Map<BspLeaf, List<Entity>> entities = new HashMap<>();
	private static Map<BspLeaf, List<Entity>> staticEntities = new HashMap<>();
	private static List<Entity> dynamicEntities = new ArrayList<>();
	
	
	
	public static Model billboard;
	
	private static Architecture arc;
	
	public EntityHandler() {
		//entities = new HashMap<>();
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
	
	public static void link(Architecture architecture) {
		arc = architecture;
	}

	public static void addEntity(Entity entity) {
		Integer leafId = new Integer(-1);
		BspLeaf leaf = arc.bsp.walk(entity.pos, leafId);
		/*List<Entity> batch = entities.get(leaf);
		
		if (batch == null) {
			batch = new ArrayList<Entity>();
			entities.put(leaf, batch);
		}
		
		batch.add(entity);*/
		
		if (entity.deactivationRange == 0f) {
			// Static
			List<Entity> batch = staticEntities.get(leaf);
			if (batch == null) {
				batch = new ArrayList<Entity>();
				staticEntities.put(leaf, batch);
			}
			batch.add(entity);
			//if (ArcHandler.) {
			//	activeLeafs.put(leaf, leafId);
			//}
		} else {
			// Dynamic
			dynamicEntities.add(entity);
		}
		entity.setLeaf(leaf);
	}
	
	public static void removeEntity(Entity entity) {
		
		if (entity.deactivationRange == 0f) {
			BspLeaf leaf = entity.getLeaf();
			List<Entity> batch = staticEntities.get(leaf);
			if (batch != null) {
				batch.remove(entity);
				if (batch.isEmpty()) {
					staticEntities.remove(leaf);
				}
			}
		} else {
			dynamicEntities.remove(entity);
		}
	}
	
	public void update(PlayableScene scene) {
		
		Vector3f camPos = scene.getCamera().getPosition();
		
		List<BspLeaf> loaded = scene.getArcHandler().getArchitecture().getRenderedLeaves();
		// List<BspLeaf> unloaded = scene.getArcHandler().getArchitecture().getUnloadedLeafs();
		
		entities.clear();
		
		// Check loaded/unloaded leaves, add/remove static entities as needed
		for(BspLeaf leaf : loaded) {
			List<Entity> batch = staticEntities.get(leaf);
			if (batch != null) {
				entities.put(leaf, batch);
			}
		}
		
		/*for(BspLeaf leaf : unloaded) {
			List<Entity> batch = staticEntities.get(leaf);
			if (leaf != null) {
				entities.remove(leaf, batch);
			}
		}*/
		
		for(Entity entity : dynamicEntities) {
			float dist = Vector3f.distanceSquared(entity.pos, camPos);
			
			if (!entity.deactivated && dist <= entity.deactivationRange * entity.deactivationRange) {
				List<Entity> batch = entities.get(entity.getLeaf());
				if (batch == null) {
					batch = new ArrayList<Entity>();
					entities.put(entity.getLeaf(), batch);
				}
				
				batch.add(entity);
			}
		}
		
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
		if (Debug.ambientOnly) {
			Resources.getTexture("none").bind(0);
		}
		
		for(BspLeaf leaf : arc.getRenderedLeaves()) {
			List<Entity> batch = entities.get(leaf);
			if (batch == null) continue;
			
			for(Entity entity : batch) {
				render(shader, camera, entity);
			}
		}
		
		GL30.glBindVertexArray(0);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		shader.stop();
	}
	
	private void render(GenericShader shader, Camera camera, Entity entity) {
		Model model = entity.getModel();
		if (model == null) 
			return;
		Texture texture = entity.getTexture();
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
		for(List<Entity> batch : staticEntities.values()) {
			for(Entity entity : batch) {
				entity.cleanUp();
			}
		}
		
		for(Entity entity : dynamicEntities) {
			entity.cleanUp();
		}
		
		staticEntities.clear();
		dynamicEntities.clear();
		billboard.cleanUp();
	}

	public static Entity getEntity(String name) {
		for(List<Entity> batch : staticEntities.values()) {
			for(Entity entity : batch) {
				
				if (entity.name.equals(name)) {
					return entity;
				}
			}
		}
		
		for(Entity entity : dynamicEntities) {
			if (entity.name.equals(name)) {
				return entity;
			}
		}
		
		return null;
	}
	
	public static List<Entity> getEntities(BspLeaf leaf) {
		return entities.get(leaf);
	}
}
