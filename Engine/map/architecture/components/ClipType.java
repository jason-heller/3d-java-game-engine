package map.architecture.components;

import org.joml.Vector3f;

import util.Colors;

public enum ClipType {
	UNKNOWN(Colors.WHITE), SOLID(Colors.RED), PLAYER_CLIP(Colors.PINK), NPC_CLIP(Colors.GREEN), LADDER(Colors.YELLOW), OBSOLETE(Colors.WHITE), ENVIRONMENT_MAP(Colors.TEAL), PROP_SPAWN(Colors.VIOLET), TRIGGER(Colors.PINK);
	
	private Vector3f color;
	
	ClipType(Vector3f color) {
		this.color = color;
	}

	public Vector3f getColor() {
		return color;
	}
}