package gl;

import org.joml.Vector3f;

public interface CameraFollowable {
	public Vector3f getViewAngle();

	public Vector3f getPosition();

	public Vector3f getRotation();
}
