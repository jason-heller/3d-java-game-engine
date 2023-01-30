package util;

import org.joml.Vector3f;

import core.App;
import gl.CameraFollowable;
import gl.Window;
import map.architecture.Architecture;
import map.architecture.util.BspRaycast;
import scene.PlayableScene;

public class ThirdPersonCameraController implements CameraFollowable {
	
	private float followDistance = 37.5f;
	private float trackingSpeed = 32f;
	
	private CameraFollowable following;
	
	private Vector3f position, direction;
	
	public ThirdPersonCameraController(CameraFollowable following) {
		this.following = following;
		
		position = new Vector3f();
		direction = new Vector3f();
	}

	@Override
	public Vector3f getViewAngle() {
		Vector3f diff = Vectors.sub(position, following.getPosition()).normalize();
		direction = direction.lerp(diff, trackingSpeed * Window.deltaTime);
		
		return direction;
	}

	@Override
	public Vector3f getPosition() {
		Vector3f target = new Vector3f(following.getPosition());
		float dirRad = following.getViewAngle().y;
		Vector3f dir = new Vector3f((float)-Math.sin(dirRad) * .7f, .2f, (float)-Math.cos(dirRad) * .7f);
		target.add(Vectors.mul(dir, followDistance));
		
		// Do collision
		Architecture arc = ((PlayableScene)App.scene).getArchitecture();
		if (arc != null) {
			BspRaycast ray = arc.raycast(following.getPosition(), dir);
			
			if (ray != null && ray.getDistance() < followDistance - 2f) {
				float raycastLength = ray.getDistance() - 2f;
				target = Vectors.add(following.getPosition(), Vectors.mul(dir, raycastLength));
			}
		}
		
		position = target.lerp(position, 10f * Window.deltaTime);
		
		return position;
	}

	public void setFollowing(CameraFollowable following) {
		this.following = following;

		position = getPosition();
		Vector3f diff = Vectors.sub(position, following.getPosition());
		direction = MathUtil.directionVectorToEuler(diff, Vectors.POSITIVE_Y);
	}
}
