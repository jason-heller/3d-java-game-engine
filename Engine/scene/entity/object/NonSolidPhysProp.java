package scene.entity.object;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import gl.Window;
import gl.res.Mesh;
import scene.PlayableScene;
import scene.entity.Spawnable;

public class NonSolidPhysProp extends HoldableEntity implements Spawnable {

	private float emfLevel = 0f;
	
	private Vector3f originalBounds = new Vector3f();
	private Vector3f torque = new Vector3f();
	
	public NonSolidPhysProp() {
		super("", new Vector3f());
		originalBounds.set(this.bbox.getBounds());
		solid = false;

		rot.set((float) Math.random() * 360f, (float) Math.random() * 360f, (float) Math.random() * 360f);
		updateBoundingBox();
	}

	public NonSolidPhysProp(Vector3f pos, Vector3f rot, String name) {
		super(name, new Vector3f());
		this.pos.set(pos);
		this.rot.set(rot);
		setModel(new String[] {name}, new String[] {name});
		bbox.getBounds().set(model.getMeshes()[0].bounds);
		originalBounds.set(this.bbox.getBounds());
		solid = false;
		
		updateBoundingBox();
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		
		updateBoundingBox();
		
		rot.add(Vector3f.mul(torque, Window.deltaTime));
		
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
		mat.rotateX(rot.x);
		mat.rotateY(rot.y);
		mat.rotateZ(rot.z);
		mat.transform(boundsX);
		mat.transform(boundsY);
		mat.transform(boundsZ);
		
		boundsX.abs();
		boundsY.abs();
		boundsZ.abs();

		bbox.getBounds().set(Math.max(boundsX.x, Math.max(boundsY.x, boundsZ.x)),
				Math.max(boundsX.y, Math.max(boundsY.y, boundsZ.y)),
				Math.max(boundsX.z, Math.max(boundsY.z, boundsZ.z)));
	}

	@Override
	public void accelerate(Vector3f direction, float magnitude) {
		super.accelerate(direction, magnitude);
		forceToTorque(Vector3f.mul(direction, magnitude));
	}
	
	private void forceToTorque(Vector3f force) {
		torque.set(force.x * 4f, force.y * 4f, force.z * 4f);
	}

	@Override
	public void release() {
		super.release();
		forceToTorque(vel);
	}
	
	@Override
	public boolean spawn(Vector3f pos, Vector3f rot, String... args) {
		if (args.length < 2)
			return false;
		
		this.pos.set(pos);
		this.name = args[1];
		setModel(new String[] {args[1]}, new String[] {args[1]});
		
		
		//if (args.length > 2)
		//	model.gettext(args[2]);
		
		bbox.getBounds().set(model.getMeshes()[0].bounds);
		originalBounds.set(this.bbox.getBounds());
		return true;
	}
	

	
	public void ghostInteraction() {
		emfLevel = 3;
	}
	
	public float getEmfLevel() {
		return emfLevel;
	}
}
