package scene.mapscene.item;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Application;
import gl.Camera;
import map.architecture.util.BspRaycast;
import scene.entity.EntityHandler;
import scene.entity.object.MotionSensorEntity;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import util.MathUtil;

public class MotionSensorItem extends Item {
	
	private int numSensors = 2;
	
	public MotionSensorItem(MapScene scene) {
		super(scene);
	}

	@Override
	public void interact() {
		if (numSensors > 0) {
			Camera c = Application.scene.getCamera();
			BspRaycast raycastData = scene.getArchitecture().raycast(c.getPosition(), c.getDirectionVector());
			if (raycastData != null && raycastData.getDistance() < 14f) {
				Vector3f normal = scene.getArchitecture().bsp.planes[raycastData.getFace().planeId].normal;
				Vector3f spawnPos = Vector3f.add(c.getPosition(),
						Vector3f.mul(c.getDirectionVector(), raycastData.getDistance() - 1f));
				EntityHandler.addEntity(
						new MotionSensorEntity(spawnPos, normal));
				// numSensors--;
			}
			
		}
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
