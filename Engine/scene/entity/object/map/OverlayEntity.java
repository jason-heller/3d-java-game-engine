package scene.entity.object.map;

import org.joml.Vector3f;

import gl.res.Mesh;
import gl.res.Texture;
import map.architecture.vis.BspLeaf;
import scene.entity.Entity;

public class OverlayEntity extends Entity {
	
	public OverlayEntity(Vector3f pos, Mesh model, Texture tex, BspLeaf leaf) {
		super("overlay");
		setModel(model, tex);
		this.pos = pos;
		this.leaf = leaf;
	}
	
	@Override
	public void setLeaf(BspLeaf leaf) {
		//this.leaf = leaf;
	}
}
