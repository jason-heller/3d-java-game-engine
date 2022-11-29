package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import geom.AxisAlignedBBox;
import gl.Window;
import gl.anim.Animator;
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import util.Colors;

public abstract class Entity {
	private static final AxisAlignedBBox NO_BOUNDINGBOX = new AxisAlignedBBox(Vector3f.ZERO, Vector3f.ZERO);
	
	public Vector3f pos = new Vector3f();
	public Vector3f rot = new Vector3f();
	private Matrix4f modelMatrix = new Matrix4f();
	public float scale = 1f;
	
	public boolean visible = true;
	
	protected Model model;
	
	protected String name;
	
	public boolean deactivated;
	public Vector3f[] lighting = new Vector3f[6];
	
	protected BspLeaf leaf;

	protected AxisAlignedBBox bbox = NO_BOUNDINGBOX;
	
	protected float deactivationRange = Float.POSITIVE_INFINITY;
	
	private Vector3f color = new Vector3f(1f, 1f, 1f);
	private float colorBlendFactor = 0f;
	
	private Animator animator;
	
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
		model = new Model(new Mesh[] {mesh}, new Texture[] {texture});
	}
	
	public String getName() {
		return name;
	}

	public void update(PlayableScene scene) {
		modelMatrix.identity();
		modelMatrix.translate(pos.x, pos.y - bbox.getHeight(), pos.z);
		modelMatrix.rotate(rot);
		modelMatrix.scale(scale);
		
		Vector3f[] targetLight = scene.getArchitecture().getLightsAt(pos);
		for(int i = 0; i < 6; i++) {
			lighting[i] = Vector3f.lerp(targetLight[i], lighting[i], 10f * Window.deltaTime);
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

	public AxisAlignedBBox getBBox() {
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
}
