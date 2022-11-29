package map.architecture.components;

import org.joml.Vector3f;

import util.Colors;

public enum ClipType {
	UNKNOWN(Colors.WHITE), SOLID(Colors.RED), PLAYER_CLIP(Colors.ORANGE), NPC_CLIP(Colors.GREEN), LADDER(Colors.YELLOW), TRIGGER(Colors.PINK), ENVIRONMENT_MAP(Colors.TEAL), PROP_SPAWN(Colors.VIOLET);
	
	private Vector3f color;
	
	ClipType(Vector3f color) {
		this.color = color;
	}

	public Vector3f getColor() {
		return color;
	}
}