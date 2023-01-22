package gl.anim.component;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;

public class Joint {

	public final byte index;
	public final String name;

	public Vector3f position;
	public Quaternion rotation;
	
	public Vector3f animPos = new Vector3f();
	public Quaternion animRot = new Quaternion();
	
	private final Matrix4f invBindMatrix;

	public final List<Joint> children = new ArrayList<Joint>();

	
	public Joint(byte index, String name, Vector3f pos, Quaternion rot) {
		this.index = index;
		this.name = name;
		this.position = pos;
		this.rotation = rot;
		
		invBindMatrix = new Matrix4f();
		invBindMatrix.translate(position);
		invBindMatrix.rotate(rotation);
		invBindMatrix.invert();
	}

	public void addChild(Joint child) {
		this.children.add(child);
	}

	public Matrix4f getInverseBindMatrix() {
		return invBindMatrix;
	}

	public Joint getJoint(String name) {
		if (this.name.equals(name)) {
			return this;
		}
		
		for(Joint child : children) {
			if (child.name.equals(name)) {
				return child.getJoint(name);
			}
		}
		
		return null;
	}
}
