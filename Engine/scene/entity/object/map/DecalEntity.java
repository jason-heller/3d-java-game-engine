package scene.entity.object.map;

import org.joml.Vector3f;

import core.App;
import core.Resources;
import dev.cmd.Console;
import geom.Plane;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.mapscene.MapScene;
import util.MathUtil;

public class DecalEntity extends Entity {
	
	public DecalEntity(Vector3f pos, String tex) {
		this(pos, Resources.getModel("cube"), Resources.getTexture(tex));
	}
	
	public DecalEntity(Vector3f pos, Model model, Texture tex) {
		super("decal");
		this.setModel(model);
		this.setTexture(tex);
		this.pos = pos;
	}
	
	@Override
	public void setLeaf(BspLeaf leaf) {
		this.leaf = leaf;
		MapScene scene = (MapScene)App.scene;
		Bsp bsp = scene.getArchitecture().bsp;
		
		float closest = Float.POSITIVE_INFINITY;
		Plane closestPlane = null;
		
		for(int i = 0; i < leaf.numFaces; i++) {
			int faceId = bsp.leafFaceIndices[leaf.firstFace + i];
			Plane plane = bsp.planes[bsp.faces[faceId].planeId];
			float dist = Math.abs(plane.signedDistanceTo(pos));
			
			if (dist < closest) {
				closest = dist;
				closestPlane = plane;
			}
		}
		
		if (closestPlane == null) {
			Console.warning("Decal unlinked to plane", leaf.numFaces);
			return;
		}

		rot.set(MathUtil.directionVectorToEuler(closestPlane.normal, Vector3f.Y_AXIS));
		pos.add(Vector3f.mul(closestPlane.normal, .1f));
		super.update(scene);
	}
	
	@Override
	public void update(PlayableScene scene) {
	}
}
