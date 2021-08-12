package map.architecture.functions.commands;

import org.joml.Vector3f;

import core.Application;
import dev.cmd.Console;
import gl.Camera;
import map.architecture.functions.ArcFunction;
import scene.entity.util.PhysicsEntity;
import scene.entity.util.PlayerHandler;

public class SpawnPoint extends ArcFunction {
	private Vector3f rot;
	
	public SpawnPoint(Vector3f pos, Vector3f rot, String name) {
		super(name, pos);
		this.rot = rot;
	}

	@Override
	public void trigger(String[] args) {
		PhysicsEntity entity = PlayerHandler.getEntity();
		Camera camera = Application.scene.getCamera();
		entity.pos.set(pos);
		Console.log(pos, rot);
		entity.vel.y = 0;
		camera.setPitch(rot.x);
		camera.setYaw(rot.y);
		camera.setRoll(rot.z);
	}
}