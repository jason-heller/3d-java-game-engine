package gl.anim.component;

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

	public Joint getJoint(String name) {
		return rootJoint.getJoint(name);
	}

	/*public void calculateJointPositions(Joint parent) {
		for(Joint child : parent.children) {
			Vector3f rotPos = Vector3f.rotateVector(child.position, parent.rotation);
			Vector3f newPos = Vectors.add(parent.position, rotPos);
			Quaternion newQuat = new Quaternion();
			Quaternion.mul(parent.rotation, child.rotation, newQuat);
			newQuat.normalize();
			child.position.set(newPos);
			child.rotation.set(newQuat);
			
			calculateJointPositions(child);
		}
	}*/
}
