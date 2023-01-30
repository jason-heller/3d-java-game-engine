package scene.entity.object;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import gl.Window;
import scene.PlayableScene;
import scene.entity.Spawnable;
import scene.entity.util.PhysicsEntity;
import util.Vectors;

public class NonSolidPhysProp extends PhysicsEntity implements Spawnable {

	private float emfLevel = 0f;
	
	private Vector3f originalBounds = new Vector3f();
	private Vector3f torque = new Vector3f();
	
	public NonSolidPhysProp() {
		super("", new Vector3f());
		originalBounds.set(this.bbox.getHalfSize());
		solid = false;

		updateBoundingBox();
	}

	public NonSolidPhysProp(Vector3f pos, Vector3f rot, String name) {
		super(name, new Vector3f());
		this.position.set(pos);
		setModel(new String[] {name}, new String[] {name});
		bbox.getHalfSize().set(model.getMeshes()[0].bounds);
		originalBounds.set(this.bbox.getHalfSize());
		solid = false;
		
		updateBoundingBox();
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		
		updateBoundingBox();
		
		if (grounded) {
			torque.mul(.9f);
			if (torque.lengthSquared() < 0.01f || vel.lengthSquared() < 0.01f) {
				torque.zero();
			}
		}

	}
	
	private void updateBoundingBox() {
		Vector3f boundsX = new Vector3f(originalBounds.x, 0f, 0f);
		Vector3f boundsY = new Vector3f(0f, originalBounds.y, 0f);
		Vector3f boundsZ = new Vector3f(0f, 0f, originalBounds.z);
		Matrix3f mat = new Matrix3f();
		mat.rotateX(rotation.x);
		mat.rotateY(rotation.y);
		mat.rotateZ(rotation.z);
		mat.transform(boundsX);
		mat.transform(boundsY);
		mat.transform(boundsZ);
		
		boundsX.absolute();
		boundsY.absolute();
		boundsZ.absolute();

		bbox.getHalfSize().set(Math.max(boundsX.x, Math.max(boundsY.x, boundsZ.x)),
				Math.max(boundsX.y, Math.max(boundsY.y, boundsZ.y)),
				Math.max(boundsX.z, Math.max(boundsY.z, boundsZ.z)));
	}

	@Override
	public void accelerate(Vector3f direction, float magnitude) {
		super.accelerate(direction, magnitude);
		forceToTorque(Vectors.mul(direction, magnitude));
	}
	
	private void forceToTorque(Vector3f force) {
		torque.set(force.x * 4f, force.y * 4f, force.z * 4f);
	}
	
	@Override
	public boolean spawn(Vector3f pos, Vector3f rot, String... args) {
		if (args.length < 2)
			return false;
		
		this.position.set(pos);
		this.name = args[1];
		setModel(new String[] {args[1]}, new String[] {args[1]});
		
		
		//if (args.length > 2)
		//	model.gettext(args[2]);
		
		bbox.getHalfSize().set(model.getMeshes()[0].bounds);
		originalBounds.set(this.bbox.getHalfSize());
		return true;
	}
	

	
	public void ghostInteraction() {
		emfLevel = 3;
	}
	
	public float getEmfLevel() {
		return emfLevel;
	}
}
