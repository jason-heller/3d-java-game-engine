package scene.mapscene.item;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.App;
import gl.Camera;
import scene.entity.EntityHandler;
import scene.entity.object.SolidPhysProp;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import util.Vectors;

public class PhysPropSpawnerItem extends Item {

	public PhysPropSpawnerItem(MapScene scene) {
		super(scene);
	}

	@Override
	public void interact() {
		Camera c = App.scene.getCamera();
		Vector3f spawnpos = Vectors.add(c.getPosition(), Vectors.mul(c.getDirectionVector(), 7f));
		EntityHandler.addEntity(new SolidPhysProp(spawnpos, c.getDirectionVector(), "default"));
	}
	
	@Override
	public void interactEnd() {
	}

	@Override
	public ViewModel setViewModel() {
		Matrix4f m = new Matrix4f();
		return new ViewModel("cube", "default", m);
	}

	@Override
	public boolean setHoldKeyToInteract() {
		return false;
	}

	@Override
	public void equip() {
	}

	@Override
	public void unequip() {
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

}
