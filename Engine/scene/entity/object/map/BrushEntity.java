package scene.entity.object.map;

import org.joml.Vector3f;

import core.Resources;
import dev.cmd.Console;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.util.ArcMeshBuilder;
import map.architecture.vis.Bsp;
import scene.entity.Entity;

public class BrushEntity extends Entity {
	
	protected Vector3f center, halfSize;
	protected int leafId;
	protected int firstFace, lastFace;

	public BrushEntity(String name) {
		super(name);
		Console.log("BRUSH");
	}

	public void setBounds(Vector3f center, Vector3f halfSize) {
		this.center = center;
		this.halfSize = halfSize;
	}

	public void setModelData(int leafId, int firstFace, int lastFace) {
		this.leafId = leafId;
		
		this.firstFace = firstFace;
		this.lastFace = lastFace;
	}
	
	public void buildModel(Architecture arc) {
		Bsp bsp = arc.bsp;
		Model model = ArcMeshBuilder.buildModel(bsp, firstFace, lastFace);
		
		arc.models.add(model);
		this.setModel(model);
		
		int diffuseId = bsp.getTextureMappings()[bsp.faces[firstFace].texMapping].textureId;
		this.getModel().setTexture(0, arc.getTextures()[diffuseId]);
	}
}
