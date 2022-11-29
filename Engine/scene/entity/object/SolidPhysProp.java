package scene.entity.object;

import org.joml.Vector3f;

import dev.cmd.Console;
import gl.res.Mesh;
import scene.entity.Spawnable;

public class SolidPhysProp extends HoldableEntity implements Spawnable {
	
	public SolidPhysProp() {
		super("", new Vector3f());
		solid = true;
	}

	public SolidPhysProp(Vector3f pos, Vector3f rot, String name) {
		super(name, new Vector3f());
		this.pos.set(pos);
		this.rot.set(rot);
		setModel(new String[] {name}, new String[] {name});
		bbox.getBounds().set(model.getMeshes()[0].bounds);

		
		solid = true;
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
		return true;
	}
}
