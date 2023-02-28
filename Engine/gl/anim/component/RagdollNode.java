package gl.anim.component;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import geom.BoundingBox;
import geom.CollideUtils;
import geom.MTV;
import gl.Window;
import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import util.Vectors;

public class RagdollNode {
	private BoundingBox box;
	private Vector3f offset;

	private Vector3f velocity;
	private Quaternionf rotation;

	public RagdollNode(Vector3f center, Vector3f offset, Vector3f bounds) {
		box = new BoundingBox(new Vector3f(center).add(offset), bounds);
		this.offset = offset;
	}

	public void update(Bsp bsp, RagdollNode[] nodes) {
		box.center.add(Vectors.mul(velocity, Window.deltaTime));
		
		BspLeaf leaf = bsp.walk(box.center);
		ArcFace[] faces = bsp.getFaces(leaf);
		 
		for(ArcFace face : faces) {
			MTV mtv = CollideUtils.faceCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, bsp.planes[face.planeId].normal, box);
			
			if (mtv != null) {
				 box.center.add(mtv.getMTV());
				 if (mtv.getAxis().y < .5f) {
					 velocity.y = 0f;
				 }
			}
		}
		
		for(RagdollNode node : nodes) {
			if (box.intersects(node.getBoundingBox())) {
				Vector3f escape = Vectors.mul(box.getIntersectionAxis(), box.getIntersectionDepth());
				box.getCenter().add(escape);
			}
		}
	}

	public void setRotation(Quaternionf rotation) {
		this.rotation = rotation;
		box.setRotation(rotation);
	}

	public Vector3f getCenter() {
		return box.center;
	}

	public BoundingBox getBoundingBox() {
		return this.box;
	}

	public void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}
	
	public Vector3f getOffset() {
		return offset;
	}

}
