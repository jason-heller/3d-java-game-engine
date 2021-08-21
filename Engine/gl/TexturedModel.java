package gl;

import org.joml.Matrix4f;

import core.Resources;
import gl.res.Model;
import gl.res.Texture;

public class TexturedModel {
	private Model model;
	private Texture texture;
	private Matrix4f matrix;
	
	public TexturedModel(Model model, Texture texture, Matrix4f matrix) {
		this.model = model;
		this.texture = texture;
		this.matrix = matrix;
	}
	
	public TexturedModel(String model, String texture, Matrix4f matrix) {
		this.model =  Resources.getModel(model);
		this.matrix = matrix;
		if (texture.equals("default")) {
			this.texture = Resources.getTexture(this.model.getMeshData().getDefaultTexture());
		} else {
			this.texture = Resources.getTexture(texture);
		}
	}
	
	// Specific constructor for BSP rendering
	public TexturedModel(Model model, String texture) {
		this.model = model;
		this.matrix = new Matrix4f();
		
		this.texture = Resources.getTexture(texture);
	}

	public Model getModel() {
		return model;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public Matrix4f getMatrix() {
		return matrix;
	}
}
