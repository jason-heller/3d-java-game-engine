package scene.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector4f;

import gl.Camera;
import gl.entity.EntityRender;
import map.architecture.ActiveLeaves;
import map.architecture.Architecture;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;

public class EntityHandler {

	private static Map<BspLeaf, List<Entity>> entities = new HashMap<>();
	private static Map<BspLeaf, List<Entity>> staticEntities = new HashMap<>();
	private static List<Entity> dynamicEntities = new ArrayList<>();
	
	private static Architecture arc;
	
	private EntityRender entityRender = new EntityRender();
	
	public EntityHandler() {
		//entities = new HashMap<>();
		entityRender = new EntityRender();
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
		
		ActiveLeaves activeLeaves = scene.getArchitecture().getActiveLeaves();
		
		entities.clear();
		
		// Check loaded/unloaded leaves, add/remove static entities as needed
		activeLeaves.beginIteration();
		while(activeLeaves.hasNext()) {
			BspLeaf leaf = activeLeaves.next();
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

	public void cleanUp() {
		for(List<Entity> batch : staticEntities.values()) {
			for(Entity entity : batch) {
				entity.cleanUp();
			}
		}
		
		for(Entity entity : dynamicEntities) {
			entity.cleanUp();
		}
		
		
		entityRender.cleanUp();
		staticEntities.clear();
		dynamicEntities.clear();
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

	public static void clear() {
		entities.clear();
	}

	public void render(Camera camera, Architecture arc, Vector4f clipPlane) {
		entityRender.render(camera, arc, clipPlane, entities);
	}

	public static List<Entity> getEntities(List<BspLeaf> leaves) {
		List<Entity> entities = new ArrayList<>();
		
		for(BspLeaf leaf : leaves) {
			List<Entity> ents = EntityHandler.getEntities(leaf);
			if (ents != null) {
				entities.addAll(ents);
			}
		}
		
		return entities;
	}

	public static Entity raycast(Vector3f origin, Vector3f direction, List<BspLeaf> leaves, Entity ignore) {
		float closest = Float.POSITIVE_INFINITY;
		Entity closestEntity = null;
		List<Entity> entities = getEntities(leaves);
		
		for(Entity entity : entities) {
			if (entity == ignore)
				continue;
			
			float dist = entity.getBBox().collide(origin, direction);
			if (dist < closest) {
				closest = dist;
				closestEntity = entity;
			}
		}
		
		return closestEntity;
	}
}
