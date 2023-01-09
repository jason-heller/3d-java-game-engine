package util;

import org.joml.Vector3f;

import gl.CameraFollowable;
import gl.Window;

public class ThirdPersonCameraController implements CameraFollowable {
	
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
		direction = Vector3f.lerp(diff, direction, 30f * Window.deltaTime);
		
		return direction;
	}

	@Override
	public Vector3f getPosition() {
		Vector3f target = new Vector3f(following.getPosition());
		float dirRad = (float) Math.toRadians(following.getViewAngle().y);
		target.add((float)-Math.sin(dirRad) * 17f, 7.5f, (float)-Math.cos(dirRad) * 17f);
		
		position = Vector3f.lerp(target, position, 10f * Window.deltaTime);
		
		return position;
	}



	public void setFollowing(CameraFollowable following) {
		this.following = following;

		position = getPosition();
		Vector3f diff = Vector3f.sub(position, following.getPosition());
		direction = MathUtil.directionVectorToEuler(diff, Vector3f.Y_AXIS);
	}
}
