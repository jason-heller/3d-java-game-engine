package scene.entity.object;

import org.joml.Vector3f;

import scene.entity.Spawnable;
import scene.entity.util.PhysicsEntity;
import util.Vectors;

public class SolidPhysProp extends PhysicsEntity implements Spawnable {
	
	public SolidPhysProp() {
		super("", new Vector3f());
		solid = true;
	}

	public SolidPhysProp(Vector3f pos, Vector3f rot, String name) {
		super(name, new Vector3f());
		this.position.set(pos);
		this.rotation.rotateXYZ(rot.x, rot.y, rot.z);
		setModel(name);
		bbox.getHalfSize().set(getModel().getBoundingBox().getBounds());

		
		solid = true;
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
		return true;
	}
}
