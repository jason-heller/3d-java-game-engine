package map.architecture.components;

import org.joml.Vector3f;

public class GhostPoi {

	private Vector3f position, rotation;
	private String command;
	
	public GhostPoi(Vector3f position, Vector3f rotation, String command) {
		this.position = position;
		this.rotation = rotation;
		this.command = command;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getRotation() {
		return rotation;
	}
	
	public String getCommand() {
		return command;
	}

}
