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
	private float trackingSpeed = 30f;
	
	private CameraFollowable following;
	
	private Vector3f position, direction;
	
	public ThirdPersonCameraController(CameraFollowable following) {
		this.following = following;
		
		position = new Vector3f();
		direction = new Vector3f();
	}

	@Override
	public Vector3f getViewAngle() {
		Vector3f diff = Vector3f.sub(position, following.getPosition()).normalize();
		direction = Vector3f.lerp(diff, direction, trackingSpeed * Window.deltaTime);
		
		return direction;
	}

	@Override
	public Vector3f getPosition() {
		Vector3f target = new Vector3f(following.getPosition());
		float dirRad = (float) Math.toRadians(following.getViewAngle().y);
		Vector3f dir = new Vector3f((float)-Math.sin(dirRad) * .4f, .2f, (float)-Math.cos(dirRad) * .4f);
		target.add(Vector3f.mul(dir, followDistance));
		
		// Do collision
		Architecture arc = ((PlayableScene)App.scene).getArchitecture();
		if (arc != null) {
			BspRaycast ray = arc.raycast(following.getPosition(), dir);
			
			if (ray != null && ray.getDistance() < followDistance - 2f) {
				float raycastLength = ray.getDistance() - 2f;
				target = Vector3f.add(following.getPosition(), Vector3f.mul(dir, raycastLength));
			}
		}
		
		position = Vector3f.lerp(target, position, 10f * Window.deltaTime);
		
		return position;
	}

	public void setTrackingSpeed(float trackingSpeed) {
		this.trackingSpeed = trackingSpeed;
	}

	public void setFollowing(CameraFollowable following) {
		this.following = following;

		position = getPosition();
		Vector3f diff = Vector3f.sub(position, following.getPosition());
		direction = MathUtil.directionVectorToEuler(diff, Vector3f.Y_AXIS);
	}
}
