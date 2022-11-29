package gl.res;

import java.util.ArrayList;
import java.util.List;

import core.Resources;
import dev.cmd.Console;

public class ModelBuilder {
	private List<Mesh> meshes;
	private List<Texture> textures;
	
	public ModelBuilder() {
		meshes = new ArrayList<>();
		textures = new ArrayList<>();
	}
	
	public void addObjMesh(String path) {
		Mesh mesh = MeshUtils.loadObj("res/" + path);
		if (mesh != null) {
			meshes.add(mesh);
		}
	}
	
	public void addTexture(String path) {
		final Texture tex = TextureUtils.createTexture("res/" + path);
		
		if (tex != null) {
			textures.add(tex);
		}
	}
	
	public Model toModel() {
		int numMeshes = meshes.size();
		
		if (numMeshes != textures.size()) {
			Console.severe("ModelBuilder error, mesh-texture mismatch");
			return Resources.ERROR;
		}
		
		Mesh[] meshArray = new Mesh[numMeshes];
		meshArray = meshes.toArray(meshArray);
		
		Texture[] texArray = new Texture[numMeshes];
		texArray = textures.toArray(texArray);
		
		
		Model model = new Model(meshArray, texArray);
		
		return model;
	}
}
