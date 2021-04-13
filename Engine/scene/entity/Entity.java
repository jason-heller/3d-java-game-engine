package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import gl.Window;
import gl.anim.Animator;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;

public abstract class Entity {
	public Vector3f pos = new Vector3f();
	public Vector3f rot = new Vector3f();
	private Matrix4f mat = new Matrix4f();
	public float scale = 1f;
	
	private Model model;
	private Texture texture;
	private Animator animator;
	
	protected String name;
	
	public boolean deactivated;
	private boolean uniqueModel = false, uniqueTexture = false;
	public Vector3f[] lighting = new Vector3f[6];
	
	protected BspLeaf leaf;
	
	protected float deactivationRange = Float.POSITIVE_INFINITY;
	
	public void setLeaf(BspLeaf leaf) {
		this.leaf = leaf;
	}

	public Entity(String name) {
		this.name = name;
		for(int i = 0; i < 6; i++) {
			lighting[i] = new Vector3f(1,1,1);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setModel(String model) {
		this.model = Resources.getModel(model);
	}
	
	public void setTexture(String texture) {
		this.texture = Resources.getTexture(texture);
	}
	
	public void setAnimator(Animator animator) {
		this.animator = animator;
	}
	
	public void setModelUnique(String key, String modelPath) {
		model = Resources.addModel(key, modelPath);
		uniqueModel = true;
	}
	
	public void setTextureUnique(String key, String texturePath) {
		texture = Resources.addTexture(key, texturePath);
		uniqueTexture = true;
	}

	public void update(PlayableScene scene) {
		mat.identity();
		mat.translate(pos);
		mat.rotate(rot);
		mat.scale(scale);
		Vector3f[] targetLight = scene.getArcHandler().getArchitecture().getLightsAt(pos);
		for(int i = 0; i < 6; i++) {
			lighting[i] = Vector3f.lerp(targetLight[i], lighting[i], 10f * Window.deltaTime);
		}
	}
	
	public Model getModel() {
		return model;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public Matrix4f getMatrix() {
		return mat;
	}
	
	public Animator getAnimator() {
		return animator;
	}

	public void cleanUp() {
		if (uniqueModel) {
			model.cleanUp();
		}
		
		if (uniqueTexture) {
			texture.delete();
		}
		
		if (animator != null) {
			animator.destroy();
		}
	}

	public BspLeaf getLeaf() {
		return leaf;
	}
}
