package scene.viewmodel;

import org.joml.Matrix4f;

import gl.TexturedModel;

public class ViewModel {
	
	private TexturedModel texturedModel;

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
}
