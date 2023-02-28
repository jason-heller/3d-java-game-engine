package scene.entity;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import core.Resources;
import dev.cmd.Console;
import geom.BoundingBox;
import gl.Window;
import gl.anim.Animator;
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import util.Colors;
import util.Vectors;

public abstract class Entity {
	private static final BoundingBox NO_BOUNDINGBOX = new BoundingBox(Vectors.ZERO, Vectors.ZERO);
	
	public Vector3f position = new Vector3f();
	public Quaternionf rotation = new Quaternionf();
	protected Matrix4f modelMatrix = new Matrix4f();
	public Vector3f scale = new Vector3f(1f, 1f, 1f);
	
	public boolean visible = true;
	
	protected Model model;
	
	protected String name;
	
	public boolean deactivated;
	protected Vector3f[] lighting = new Vector3f[6];
	
	protected BspLeaf leaf;

	protected BoundingBox bbox = NO_BOUNDINGBOX;
	
	protected float deactivationRange = Float.POSITIVE_INFINITY;
	
	private Vector3f color = new Vector3f(1f, 1f, 1f);
	private float colorBlendFactor = 0f;
	
	protected Animator animator;
	
	public void setLeaf(BspLeaf leaf) {
		this.leaf = leaf;
	}

	public Entity(String name) {
		this.name = name;
		for(int i = 0; i < 6; i++) {
			lighting[i] = new Vector3f(1,1,1);
		}
	}
	
	public void setModel(String key) {
		this.model = Resources.getModel(key);
	}
	
	public void setModel(String[] meshRefs, String[] textureRefs) {
		int numMeshes = meshRefs.length;
		Mesh[] meshes = new Mesh[numMeshes];
		Texture[] textures = new Texture[numMeshes];
		
		boolean loadFailed = false;
		
		for(int i = 0; i < numMeshes; i++) {
			meshes[i] = Resources.getMesh(meshRefs[i]);
			textures[i] = Resources.getTexture(textureRefs[i]);	
			if (meshes[i] == Resources.ERROR.getMeshes()[0]) {
				loadFailed = true;
				break;
			}
		}
		
		model = loadFailed ? Resources.ERROR : new Model(meshes, textures);
	}
	
	public void setModel(Mesh mesh, Texture texture) {
		if (mesh == Resources.ERROR.getMeshes()[0])
			model = Resources.ERROR;
		else
			model = new Model(new Mesh[] {mesh}, new Texture[] {texture});
	}
	
	public String getName() {
		return name;
	}

	public void update(PlayableScene scene) {
		modelMatrix.identity();
		modelMatrix.translate(position.x, position.y - bbox.getHeight(), position.z);
		modelMatrix.rotate(rotation);
		modelMatrix.scale(scale.x, scale.y, scale.z);
		
		bbox.setRotation(rotation);

		Vector3f[] targetLight = scene.getArchitecture().getLightsAt(position);
		for (int i = 0; i < 6; i++) {
			lighting[i] = new Vector3f(targetLight[i]);
			lighting[i].lerp(lighting[i], 10f * Window.deltaTime);
		}

		if (animator != null)
			animator.update();
		
		if (model == Resources.ERROR) {
			setColor(Colors.alertColor());
		}
	}

	public Model getModel() {
		return model;
	}
	
	public Matrix4f getMatrix() {
		return modelMatrix;
	}

	public void cleanUp() {
		if (animator != null) {
			animator.destroy();
		}
	}

	public BspLeaf getLeaf() {
		return leaf;
	}
	
	public Animator getAnimator() {
		return animator;
	}

	public BoundingBox getBBox() {
		return bbox;
	}
	
	public Vector3f getColor() {
		return color;
	}

	public float getColorBlendFactor() {
		return colorBlendFactor;
	}
	
	public void setColor(Vector3f color) {
		this.setColor(color, 0.5f);
	}

	public void setColor(Vector3f color, float colorBlendFactor) {
		this.color = color;
		this.colorBlendFactor = colorBlendFactor;
	}
	
	public void setAnimator(Animator animator) {
		this.animator = animator;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Vector3f[] getLighting() {
		return lighting;
	}
}
