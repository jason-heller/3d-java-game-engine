package scene.viewmodel;

import org.joml.Matrix4f;

import gl.TexturedModel;
import gl.res.mesh.MeshTag;

public class ViewModel {
	
	protected TexturedModel texturedModel;

	public ViewModel(String model, String texture, Matrix4f matrix) {
		texturedModel = new TexturedModel(model, texture, matrix);
	}

	public TexturedModel getTexturedModel() {
		return texturedModel;
	}

	public void update() {
	}

	public void holster() {
		
	}
	
	public void equip() {
		
	}
	
	protected MeshTag getTag(int i) {
		return texturedModel.getModel().getMeshData().getTags().get(i);
	}
}
