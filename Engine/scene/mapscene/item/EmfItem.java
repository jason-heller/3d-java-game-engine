package scene.mapscene.item;

import java.util.List;

import org.joml.Vector3f;

import geom.AxisAlignedBBox;
import gl.Camera;
import gl.res.mesh.MeshData;
import map.architecture.vis.BspLeaf;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.hostile.GhostEntity;
import scene.entity.object.SolidPhysProp;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import scene.viewmodel.ViewModelEmf;

public class EmfItem extends Item {
	
	private static final float RANGE_SQR = 50*50;
	
	public EmfItem(MapScene scene) {
		super(scene);
		MeshData.setField("emf", "emf");
	}

	@Override
	public void interact() {
		
	}
	
	@Override
	public void interactEnd() {
	}
	
	@Override
	public void update() {
		GhostEntity ghost = scene.getGhost();
		float distToPlayerSqr = Vector3f.distanceSquared(ghost.pos, scene.getPlayer().pos);
		if (distToPlayerSqr <= RANGE_SQR) {// && ghost.raycastToPlayer(scene.getArchitecture())
		}
		
		Camera cam = scene.getCamera();
		AxisAlignedBBox box = new AxisAlignedBBox(cam.getPosition(), new Vector3f(5,5,5));
		List<BspLeaf> leaves = scene.getArchitecture().getVisibleLeavesIntersecting(box);
		Entity entity = EntityHandler.raycast(cam.getPosition(), cam.getDirectionVector(), leaves, scene.getPlayer());

		if (entity instanceof SolidPhysProp) {
			SolidPhysProp decorEnt = (SolidPhysProp)entity;
			
			((ViewModelEmf)viewModel).setNumEmfLights((int)decorEnt.getEmfLevel());
		} else {
			((ViewModelEmf)viewModel).setNumEmfLights(0);
		}
	}

	@Override
	public ViewModel setViewModel() {
		return new ViewModelEmf();
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

}
