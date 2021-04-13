package scene.entity;

import org.joml.Vector3f;

public interface Spawnable {	// NOTE: Spawnables need to have an empty constructor and be Entities
	
	public boolean spawn(Vector3f pos, Vector3f rot, String... args);
}
