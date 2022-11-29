package map.architecture;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import gl.Camera;
import map.architecture.vis.BspLeaf;

public class ActiveLeaves {

	public static int cutoffDist = 200;
	
	private int interationIndex = 0;
	
	private List<BspLeaf> activeLeavesNear = new ArrayList<BspLeaf>();
	private List<BspLeaf> activeLeavesFar = new ArrayList<BspLeaf>();

	public void addLeaf(Camera camera, BspLeaf leaf) {
		Vector3f camPos = camera.getPosition();
		Vector3f boxPos = Vector3f.add(leaf.max, leaf.min).mul(0.5f);
		float distSqr = Vector3f.distanceSquared(camPos, boxPos);
		
		if (distSqr < cutoffDist * cutoffDist) {
			activeLeavesNear.add(leaf);
		} else {
			activeLeavesFar.add(leaf);
		}
	}
	
	public void clear() {
		activeLeavesNear.clear();
		activeLeavesFar.clear();
	}

	public void beginIteration() {
		interationIndex = 0;
	}
	
	public boolean hasNext() {
		return interationIndex < activeLeavesNear.size() + activeLeavesFar.size();
	}

	public BspLeaf next() {
		BspLeaf leaf = interationIndex >= activeLeavesFar.size()
				? activeLeavesNear.get(interationIndex - activeLeavesFar.size())
				: activeLeavesFar.get(interationIndex);
		interationIndex++;
		return leaf;
	}

	public List<BspLeaf> getNear() {
		return activeLeavesNear;
	}

	public List<BspLeaf> getFar() {
		return activeLeavesFar;
	}
}
