package map.architecture.functions.commands;

import org.joml.Vector3f;

import core.App;
import dev.cmd.Console;
import gl.Camera;
import map.architecture.functions.ArcFunction;
import scene.PlayableScene;
import scene.entity.util.SkatePhysicsEntity;

public class SpawnPoint extends ArcFunction {
	private Vector3f rot;
	
	public SpawnPoint(Vector3f pos, Vector3f rot, String name) {
		super(name, pos);
		rot.y += 90f;
		this.rot = rot.mul((float)Math.PI / 180f);
	}

	@Override
	public void trigger(String[] args) {
		SkatePhysicsEntity entity = ((PlayableScene)App.scene).getPlayer();
		Camera camera = App.scene.getCamera();
		entity.position.set(pos);
		entity.rotation.rotateXYZ(rot.x, rot.y, rot.z);
		entity.localVelocity.y = 0;
		camera.setPitch(rot.x);
		camera.setYaw(rot.y);
		camera.setRoll(rot.z);
	}
}