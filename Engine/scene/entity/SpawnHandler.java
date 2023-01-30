package scene.entity;

import org.joml.Vector3f;

import core.App;
import dev.cmd.Console;
import geom.CollideUtils;
import gl.Camera;
import map.architecture.Architecture;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import scene.entity.object.NonSolidPhysProp;
import scene.entity.object.SolidPhysProp;
import util.Vectors;

public enum SpawnHandler {
	PROP_SOLID(SolidPhysProp.class),
	PROP_NONSOLID(NonSolidPhysProp.class),
	TEST(DummyEntity.class);
	
	private Class<? extends Spawnable> entClass;
	
	SpawnHandler(Class<? extends Spawnable> entClass, String...args) {
		this.entClass = entClass;
	}
	
	public Class<? extends Spawnable> getEntityClass() {
		return entClass;
	}
	
	public static void spawn(Architecture arc, Camera camera, String... args) {
		if (!(App.scene instanceof PlayableScene)) {
			Console.log("Failed to spawn entity, must be in playable scene");
			return;
		}
		
		Bsp bsp = arc.bsp;
		Vector3f rot = new Vector3f(camera.getYaw(), camera.getPitch(), camera.getRoll());
		float ray = CollideUtils.raycast(arc.getActiveLeaves(), bsp, camera.getPosition(), camera.getDirectionVector());
		if (ray == Float.POSITIVE_INFINITY) {
			Console.log("Failed to spawn entity, invalid position");
			return;
		}
		
		Vector3f pos = Vectors.add(camera.getPosition(), Vectors.mul(camera.getDirectionVector(), ray - 2f));
		spawn(pos, rot, args);
	}
	
	public static void spawn(Vector3f pos, Vector3f rot, String...args) {
		String name = args[0].toUpperCase();
		SpawnHandler[] spawnables = SpawnHandler.values();
		for(int i = 0; i < spawnables.length; i++) {
			if (spawnables[i].name().equals(name)) {
				try {
					Spawnable s = spawnables[i].getEntityClass().newInstance();
					boolean success = s.spawn(pos, rot, args);
					if (success) {
						//PlayableScene scene = (PlayableScene)Application.scene;
						//Bsp bsp = scene.getArcHandler().getArchitecture().bsp;
						
						Entity entity = (Entity) s;
						//entity.setLeaf(bsp.walk(pos));
						EntityHandler.addEntity(entity);
						
						Console.log(((Entity)s).position);
					} else {
						Console.log("Failed to spawn " + name + ", bad arguments.");
					}
				} catch (InstantiationException | IllegalAccessException e) {
					Console.log("Failed to spawn " + name);
					e.printStackTrace();
				}
				return;
			}
		}
		
		Console.log("Failed to spawn " + name + ", no such spawnable entity");
	}
}
