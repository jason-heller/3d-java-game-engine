package map.architecture.functions;

import org.joml.Vector3f;

/**
 * 'Functions' w.r.t. Architectures are effectively static entities/objects,
 * which only update when triggered/called via a ArcCommand. They have
 * name & positions within the map, and can contain other data in addition. They
 * are contained within the Architecture class
 */
public abstract class ArcFunction {
	protected String name;
	protected Vector3f pos;
	
	public ArcFunction(String name, Vector3f pos) {
		this.name = name;
		this.pos = pos;
	}

	public abstract void trigger(String[] args);
	
	public String getName() {
		return name;
	}
	
	public Vector3f getPosition() {
		return pos;
	}
}
