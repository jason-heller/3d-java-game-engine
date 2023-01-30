package gl.skybox._3d;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Pvs;
import scene.entity.Entity;

public class SkyboxCamera extends Entity {

	float scale;
	private float fogStart, fogEnd;
	private boolean hasFog;
	private Vector3f fogColor;
	
	private BspLeaf cameraLeaf;
	private List<BspLeaf> renderedLeaves = new ArrayList<BspLeaf>();
	
	public SkyboxCamera(Vector3f position, float scale, boolean hasFog, float fogStart, float fogEnd,
			Vector3f fogColor) {
		super("sky_camera");
		this.position = position;
		this.scale = scale / 4f;		// oops
		this.hasFog = hasFog;
		this.fogColor = fogColor;
		this.fogStart = fogStart;
		this.fogEnd = fogEnd;
	}
	
	public void updateLeaf(Bsp bsp, Pvs pvs) {
		cameraLeaf = bsp.walk(position);
		int[] vis = pvs.getData(cameraLeaf, 0);
		
		for(int i = 0; i < bsp.leaves.length; i++) {
			BspLeaf leaf = bsp.leaves[i];
			if (leaf.clusterId == -1) continue;
			if (vis[leaf.clusterId] == 0) continue;
			renderedLeaves.add(leaf);
		}
	}
	
	public List<BspLeaf> getRenderedLeaves() {
		return renderedLeaves;
	}

	public float getScale() {
		return scale;
	}

	public float getFogStart() {
		return fogStart;
	}

	public float getFogEnd() {
		return fogEnd;
	}

	public boolean isHasFog() {
		return hasFog;
	}

	public Vector3f getFogColor() {
		return fogColor;
	}
}
