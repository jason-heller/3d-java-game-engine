package map.architecture.functions.commands;

import org.joml.Vector3f;

import core.Application;
import gl.Camera;
import map.architecture.functions.ArcFunction;
import scene.Scene;

public class CamView extends ArcFunction {
	private Vector3f rot;
	private Vector3f lastRenderPos, lastRenderRot;
	
	public static Vector3f renderPos;
	public static Vector3f renderRot;
	
	public static boolean requestRender = false;
	
	public CamView(Vector3f pos, Vector3f rot) {
		super("cam_view", pos);
		this.rot = new Vector3f(rot.x, rot.y, rot.z + 180);
		lastRenderPos = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		lastRenderRot = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	@Override
	public void trigger(String[] args) {
		Scene scene = Application.scene;
		Camera camera = scene.getCamera();
		Vector3f camPos = camera.getPosition();
		Vector3f camRot = new Vector3f(camera.getYaw(), camera.getPitch(), camera.getRoll());
		
		if (!(camRot.equals(lastRenderRot) && camPos.equals(lastRenderPos))) {
			lastRenderRot.set(camRot);
			lastRenderPos.set(camPos);
			renderPos = pos;
			renderRot = rot;

			requestRender = true;
		}
	}
}