package scene.mapscene.item;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Application;
import gl.Camera;
import scene.entity.EntityHandler;
import scene.entity.object.SolidPhysProp;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;

public class PhysPropSpawnerItem extends Item {
	
	public PhysPropSpawnerItem(MapScene scene) {
		super(scene);
	}

	@Override
	public void interact() {
		Camera c = Application.scene.getCamera();
		Vector3f spawnpos = Vector3f.add(c.getPosition(), Vector3f.mul(c.getDirectionVector(), 7f));
		EntityHandler.addEntity(new SolidPhysProp(spawnpos, c.getDirectionVector(), "trash"));
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
