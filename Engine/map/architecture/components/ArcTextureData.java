package map.architecture.components;

import java.util.List;

import core.Resources;
import dev.cmd.Console;
import gl.res.Texture;
import map.architecture.Architecture;

public class ArcTextureData {
	private Texture[] textureDatas;
	private String[] textureNames;		// Indexed the same, by texdata (i think?)
	
	private Architecture parent;
	public int numToolTextures = 0;
	
	private String skybox;
	
	public ArcTextureData(Architecture parent) {
		this.parent = parent;
	}
	
	public void setSkybox() {

		if (!skybox.equals("")) {
			final String root = "sky/";
			Resources.addCubemap("skybox", root + skybox + ".png");
			parent.hasSkybox = true;
		}
	}
	
	public String[] getTextureNames() {
		return textureNames;
	}

	public void setSkybox(String skybox) {
		this.skybox = skybox;
	}

	public void setTextureData(List<Texture> textureDataList, String[] textureNames) {
		textureDatas = new Texture[textureDataList.size()];
		for(int i = 0; i < textureDatas.length; i++) {
			textureDatas[i] = textureDataList.get(i);
		}
		
		this.textureNames = textureNames;
	}

	public Texture[] getTextures() {
		return textureDatas;
	}
}
