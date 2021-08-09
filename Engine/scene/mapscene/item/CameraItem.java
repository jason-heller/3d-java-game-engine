package scene.mapscene.item;

import org.joml.Matrix4f;

import audio.AudioHandler;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import ui.UI;

public class CameraItem extends Item {
	
	public CameraItem(MapScene scene) {
		super(scene);
	}

	@Override
	public void interact() {
		// Todo: photo
		AudioHandler.play("camera_snap");
	}
	
	@Override
	public void interactEnd() {
	}

	@Override
	public ViewModel setViewModel() {
		Matrix4f m = new Matrix4f();
		return new ViewModel("camera", "default", m);
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
