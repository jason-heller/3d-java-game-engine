package gl.anim.component;

import org.joml.Quaternion;
import org.joml.Vector3f;

public class Skeleton {
	
	private final int numJoints;
	private final Joint rootJoint;

	public Skeleton(int numJoints, Joint rootJoint) {
		this.numJoints = numJoints;
		this.rootJoint = rootJoint;
	}

	public int getNumJoints() {
		return numJoints;
	}

	public Joint getRootJoint() {
		return rootJoint;
	}

	/*public void calculateJointPositions(Joint parent) {
		for(Joint child : parent.children) {
			Vector3f rotPos = Vector3f.rotateVector(child.position, parent.rotation);
			Vector3f newPos = Vector3f.add(parent.position, rotPos);
			Quaternion newQuat = new Quaternion();
			Quaternion.mul(parent.rotation, child.rotation, newQuat);
			newQuat.normalize();
			child.position.set(newPos);
			child.rotation.set(newQuat);
			
			calculateJointPositions(child);
		}
	}*/
}
