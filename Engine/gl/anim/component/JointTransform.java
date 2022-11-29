package gl.anim.component;

import org.joml.Quaternion;
import org.joml.Vector3f;

public class JointTransform {

	private Vector3f position;
	private Quaternion rotation;

	public JointTransform(Vector3f position, Quaternion rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	
	public static JointTransform lerp(JointTransform frameA, JointTransform frameB, float progression) {
		final Vector3f pos = interpolate(frameA.position, frameB.position, progression);
		final Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression);
		return new JointTransform(pos, rot);
	}
	
	private static Vector3f interpolate(Vector3f start, Vector3f end, float progression) {
		final float x = start.x + (end.x - start.x) * progression;
		final float y = start.y + (end.y - start.y) * progression;
		final float z = start.z + (end.z - start.z) * progression;
		return new Vector3f(x, y, z);
	}

	public Vector3f getPosition() {
		return position;
	}
	
	public Quaternion getRotation() {
		return rotation;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	public void setRotation(Quaternion rotation) {
		this.rotation = rotation;
	}
}
