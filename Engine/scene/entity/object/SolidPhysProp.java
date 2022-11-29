package scene.entity.object;

import org.joml.Vector3f;

import dev.cmd.Console;
import gl.res.Model;
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
		this.setModel(name);
		this.setTexture(name);
		Model model = this.getModel();
		bbox.getBounds().set(model.bounds);

		
		solid = true;
	}
	
	@Override
	public boolean spawn(Vector3f pos, Vector3f rot, String... args) {
		if (args.length < 2)
			return false;
		
		this.pos.set(pos);
		this.name = args[1];
		this.setModel(args[1]);
		this.setTexture(args[1]);
		
		if (args.length > 2)
			this.setTexture(args[2]);
		
		Model model = this.getModel();
		
		if (model == null)
			return false;
		
		bbox.getBounds().set(model.bounds);
		return true;
	}
}
