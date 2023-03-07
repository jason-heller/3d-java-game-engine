package util;

import org.joml.Vector3f;

import core.App;
import gl.CameraFollowable;
import gl.Window;
import map.architecture.Architecture;
import map.architecture.util.BspRaycast;
import scene.PlayableScene;

public class ThirdPersonCameraController implements CameraFollowable {
	
	public static float followDistance = 21f;
	
	private CameraFollowable following;
	
	private Vector3f position, targetPos, lastPosition;
	private Vector3f rotation, targetRot, lastRotation;
	public static float interp;

	private float cameraRaise = 0.25f;
	
	public ThirdPersonCameraController(CameraFollowable following) {
		this.following = following;
		
		targetPos = new Vector3f();
		targetRot = new Vector3f();
		lastPosition = new Vector3f();
		lastRotation = new Vector3f();
	}

	@Override
	public Vector3f getViewAngle() {
		return rotation;
	}

	@Override
	public Vector3f getPosition() {
		float yawRad = following.getViewAngle().x;
		float pitchRad = -MathUtil.HALFPI + following.getViewAngle().y;
		
		Vector3f dir = new Vector3f(0, 1, 0);
		dir.rotateX(pitchRad + cameraRaise);
		dir.rotateY(yawRad);
		
		Vector3f newTargetPos = new Vector3f(dir);
		newTargetPos.mul(followDistance);
		
		// Do collision
		Architecture arc = ((PlayableScene)App.scene).getArchitecture();
		if (following.getViewAngle().y < .9f && arc != null) {
			BspRaycast ray = arc.raycast(following.getPosition(), dir);
			
			if (ray != null && ray.getDistance() < followDistance - 5f) {
				float raycastLength = ray.getDistance() - 5f;
				newTargetPos.set(dir).mul(raycastLength);
			}
		}
		
		float lookDiff = newTargetPos.distanceSquared(lastPosition);
		if (lookDiff > 0f) {
			if (position == null)
				position = new Vector3f(newTargetPos).add(following.getPosition());

			lastPosition.set(Vectors.sub(position, following.getPosition()));
			targetPos.set(newTargetPos);
			
			interp = 0f;
			
			Vector3f newTargetRot = new Vector3f(newTargetPos).normalize();
			if (rotation == null) {
				rotation = new Vector3f(newTargetRot);
				targetRot.set(newTargetRot);
			}

			lastRotation.set(rotation);
			targetRot.set(newTargetRot);
		}
		
		interp = Math.min(interp + Window.deltaTime * 7f, 1f);
		
		position.set(following.getPosition());
		position.add(lastPosition.lerp(targetPos, interp));
		rotation.set(lastRotation.lerp(targetRot, interp));
		
		return position;
	}

	public void setFollowing(CameraFollowable following) {
		this.following = following;
	}
}
