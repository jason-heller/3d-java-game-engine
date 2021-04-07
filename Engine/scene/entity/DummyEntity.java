package scene.entity;

import org.joml.Vector3f;

public class DummyEntity extends Entity {

	public DummyEntity(Vector3f pos) {
		super("dummy");
		this.pos.set(pos);
		this.setModel("cube");
		this.setTexture("default");
	}

}
