package gl.res;

import geom.AABB;
import gl.anim.component.Skeleton;

/*
 * A model is a collection of meshes, textures, and an animation / armature. Not to be confused with a mesh which is just a singular vertex structure
 */
public class Model {
	
	private Mesh[] meshes;
	private Texture[] textures;
	
	private Skeleton skeleton;
	
	private AABB boundingBox;

	
	public Model(Mesh[] meshes, Texture[] textures) {
		this.meshes = meshes;
		this.textures = textures;
	}

	public Model(int numMeshes) {
		this.meshes = new Mesh[numMeshes];
		this.textures = new Texture[numMeshes];
	}
	
	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}
	
	public Mesh[] getMeshes() {
		return meshes;
	}
	
	public Texture[] getTextures() {
		return textures;
	}
	
	public Skeleton getSkeleton() {
		return skeleton;
	}

	public void setMesh(int i, Mesh mesh) {
		meshes[i] = mesh;
	}

	public void setTexture(int i, Texture texture) {
		textures[i] = texture;
	}

	public void setBoundingBox(AABB boundingBox) {
		this.boundingBox = boundingBox;
	}
	
	public AABB getBoundingBox() {
		return boundingBox;
	}
}
