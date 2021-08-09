package map.architecture.components;

import java.util.ArrayList;
import java.util.List;

import core.Resources;
import dev.Console;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.vis.Bsp;

public class ArcPackedAssets {
	private List<ArcPackedModel> models;
	private List<ArcPackedTexture> textures;
	private List<String> texReferences;

	private ArcTextureData[] texData;
	
	private Architecture parent;
	public int numToolTextures = 0;
	
	public String skybox;
	
	public ArcPackedAssets(Architecture parent) {
		models = new ArrayList<>();
		textures = new ArrayList<>();
		texReferences = new ArrayList<>();
		
		this.parent = parent;
	}
	
	public void add(ArcPackedModel model) {
		models.add(model);
	}
	
	public void add(String texReference, ArcPackedTexture texture) {
		textures.add(texture);
		texReferences.add(texReference);
	}
	
	public void passToOpenGL() {
		Bsp bsp = parent.bsp;
		Texture[] mapSpecificTextures = new Texture[textures.size() + 1];
		int j = 1;

		if (!skybox.equals("")) {
			final String root = "sky/";
			Console.log(root + skybox + ".png");
			Resources.addCubemap("skybox", root + skybox + ".png");
			parent.hasSkybox = true;
		}
		
		for(ArcPackedModel model : models) {
			model.passToOpenGL();
		}
		
		for(ArcPackedTexture texture : textures) {
			mapSpecificTextures[j++] = texture.passToOpenGL();
		}
		
		models.clear();
		textures.clear();
		
		String[] texRefsArr = new String[texReferences.size() + numToolTextures];
		for(int i = 0; i < numToolTextures; i++) {
			texRefsArr[i] = "default";
		}
		
		for(int i = numToolTextures; i < texRefsArr.length; i++) {
			texRefsArr[i] = texReferences.get(i - numToolTextures);
		}

		for(int i = 0; i < bsp.clusters.length; ++i) {
			bsp.clusters[i].buildModel(bsp.planes, bsp.edges, bsp.surfEdges, bsp.vertices, bsp.faces, bsp.leafFaceIndices, texData, texRefsArr);
		}
		
		for(int i = 0; i < bsp.heightmaps.length; i++) {
			bsp.heightmaps[i].buildModel(bsp.heightmapVerts, bsp.faces, bsp.planes, bsp.edges, bsp.surfEdges, bsp.vertices, texData, texRefsArr);
		}
		
		parent.setMapSpecificTextures(mapSpecificTextures, texRefsArr);
	}
	
	public List<String> getTextureRefs() {
		return this.texReferences;
	}
	
	public void setTextureData(ArcTextureData[] texData) {
		this.texData = texData;
	}

	public ArcTextureData[] getTextureData() {
		return texData;
	}
}
