package scene.entity.object.map;

import org.joml.Vector3f;

import core.App;
import core.Resources;
import dev.cmd.Console;
import geom.Plane;
import gl.res.Mesh;
import gl.res.Texture;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.mapscene.MapScene;
import util.MathUtil;
import util.Vectors;

public class DecalEntity extends Entity {
	
	public DecalEntity(Vector3f pos, String tex) {
		this(pos, Resources.getMesh("cube"), Resources.getTexture(tex));
	}
	
	public DecalEntity(Vector3f pos, Mesh model, Texture tex) {
		super("decal");
		setModel(model, tex);
		this.position = pos;
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
			float dist = Math.abs(plane.signedDistanceTo(position));
			
			if (dist < closest) {
				closest = dist;
				closestPlane = plane;
			}
		}
		
		if (closestPlane == null) {
			Console.warning("Decal unlinked to plane", leaf.numFaces);
			return;
		}

		//rot.set(MathUtil.directionVectorToEuler(closestPlane.normal, Vectors.POSITIVE_Y));
		position.add(Vectors.mul(closestPlane.normal, .1f));
		super.update(scene);
	}
	
	@Override
	public void update(PlayableScene scene) {
	}
}
