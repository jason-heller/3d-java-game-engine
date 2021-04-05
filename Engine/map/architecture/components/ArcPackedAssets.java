package map.architecture.components;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import core.Resources;
import geom.Plane;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.vis.BspLeaf;

public class ArcPackedAssets {
	private List<ArcPackedModel> models;
	private List<ArcPackedTexture> textures;
	private List<String> texReferences;
	
	private BspLeaf[] clusters;
	private Plane[] planes;
	private ArcEdge[] edges;
	private int[] surfEdges;
	private Vector3f[] verts;
	private ArcFace[] faces;
	private short[] leafIds;
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
	
	public void add(BspLeaf[] clusters, Plane[] planes, ArcEdge[] edges, int[] surfEdges, Vector3f[] verts,
			ArcFace[] faces, short[] leafIds, ArcTextureData[] texData) {
		this.clusters = clusters;
		this.planes = planes;
		this.edges = edges;
		this.surfEdges = surfEdges;
		this.verts = verts;
		this.faces = faces;
		this.leafIds = leafIds;
		this.texData = texData;
	}
	
	public void passToOpenGL() {
		
		Texture[] mapSpecificTextures = new Texture[textures.size() + 1];
		int j = 1;
		
		// Load Skybox
		/*final String[] paths = new String[] {"lft", "rgt", "top", "btm", "fnt", "bck"};
		final String root = "sky/" + skybox + "/" + skybox + '_';
		for(int i = 0; i < 6; i++) {
			paths[i] = root + paths[i] + ".png";
		}*/
		
		if (!skybox.equals("")) {
			final String root = "sky/";
			Resources.addCubemap("skybox", root + skybox + ".png");
			parent.hasSkybox = true;
		}
		//
		
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
		
		for(int i = 0; i < clusters.length; ++i) {
			clusters[i].buildModel(planes, edges, surfEdges, verts, faces, leafIds, texData, texRefsArr);
		}
		
		parent.setMapSpecificTextures(mapSpecificTextures, texRefsArr);
	}
	
	public ArcTextureData[] getTextureData() {
		return this.texData;
	}
	
	public List<String> getTextureRefs() {
		return this.texReferences;
	}
}
