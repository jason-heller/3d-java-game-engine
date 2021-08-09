package map.architecture.components;

import org.joml.Vector3f;

import util.Colors;

public enum ClipType {
	UNKNOWN(Colors.WHITE), GRATE(Colors.BLUE), PLAYER_CLIP(Colors.ORANGE), NPC_CLIP(Colors.GREEN), LADDER(Colors.YELLOW), TRIGGER(Colors.PINK), SOLID(Colors.RED);
	
	private Vector3f color;
	
	ClipType(Vector3f color) {
		this.color = color;
	}

	public Vector3f getColor() {
		return color;
	}
}