package scene.entity;

import org.joml.Vector3f;

public class DummyEntity extends Entity implements Spawnable {

	public DummyEntity() {
		super("dummy");
		setModel(new String[] {"cube"}, new String[] {"default"});
	}

	@Override
	public boolean spawn(Vector3f pos, Vector3f rot, String... args) {
		this.position.set(pos);
		return true;
	}

}
