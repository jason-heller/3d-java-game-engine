package gl.anim;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import geom.BoundingBox;
import gl.anim.component.Joint;
import gl.anim.component.JointTransform;
import gl.anim.component.RagdollNode;
import gl.anim.component.Skeleton;
import gl.line.LineRender;
import map.architecture.vis.Bsp;
import scene.entity.Entity;
import util.Colors;
import util.Vectors;

public class Ragdoll {
	private final Map<Byte, JointTransform> transforms;
	
	private RagdollNode[] nodes;
	private int[] parents;
	
	private Vector3f worldPos;

	public Ragdoll(Vector3f worldPos, Vector3f velocity, Skeleton skeleton, Entity entity) {
		transforms = new HashMap<>();
		nodes = new RagdollNode[skeleton.getNumJoints()];
		parents = new int[skeleton.getNumJoints()];
		parents[0] = 0;
		this.worldPos = worldPos;

		populateTransforms(worldPos, skeleton.getRootJoint());
		
		for(int i = 0; i < skeleton.getNumJoints(); i++) {
			nodes[i].setVelocity(velocity);
		}
	}
	
	private void populateTransforms(Vector3f worldPos, Joint joint) {
		final byte index = joint.index;
		Vector3f bonePos = joint.animPos;
		Quaternionf boneRot = joint.animRot;
		
		JointTransform transform = new JointTransform(bonePos, boneRot);
		transforms.put(index, transform);
		
		nodes[index] = new RagdollNode(bonePos, worldPos, new Vector3f(.5f,.5f,.5f));
		nodes[index].setRotation(boneRot);
		
		for(Joint child : joint.children) {
			populateTransforms(worldPos, child);
			parents[child.index] = index;
		}
	}

	public void update(Bsp bsp) {
		//transforms.get((byte)3).getRotation().rotateY(Window.deltaTime);
		//nodes[3].setRotation(transforms.get((byte)3).getRotation());
			
		for(int i = 0; i < nodes.length; i++) {
			RagdollNode node = nodes[i];
			node.update(bsp, nodes);
			
			JointTransform transform = transforms.get((byte)i);
			transform.setPosition(Vectors.sub(node.getCenter(), node.getOffset()));
			
			BoundingBox bbox = node.getBoundingBox();
			LineRender.drawBox(bbox, Colors.WHITE);
			LineRender.drawLine(bbox.center, nodes[parents[i]].getCenter());
		}
	}
	
	public Map<Byte, JointTransform> getTransforms() {
		return transforms;
	}

	public JointTransform getTransform(byte index) {
		return transforms.get(index);
	}

	public void applyToPose(Matrix4f[] pose, Joint parent) {
		for(Joint child : parent.children) {
			JointTransform transform = transforms.get(child.index);
			
			//Vector3f rotPos = rotate(parent.animRot, transform.getPosition()); // get parents position, after rotation
			//Vector3f newPos = Vectors.add(parent.animPos, rotPos); // add the parents position to this joints position
			
			Quaternionf newRot = new Quaternionf(parent.animRot); 
			newRot.mul(transform.getRotation());
			
			Matrix4f matrix = new Matrix4f();
			matrix.translate(transform.getPosition());
			matrix.rotate(transform.getRotation());
			matrix.mul(child.getInverseBindMatrix());
			pose[child.index] = matrix;
			
			applyToPose(pose, child);
		}
	}
	
	private Vector3f rotate(Quaternionf q, Vector3f v) {
		Vector3f quatVector = new Vector3f(q.x, q.y, q.z);
		Vector3f uv = Vectors.cross(quatVector, v);
		Vector3f uuv = Vectors.cross(quatVector, uv);
		return Vectors.add(v, Vectors.add(Vectors.mul(uv, q.w), uuv).mul(2f));
	}
}