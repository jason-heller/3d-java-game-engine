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
		this.texture = Resources.getTexture(texture);
		this.matrix = matrix;
	}
	
	// Specific constructor for BSP rendering
	public TexturedModel(Model model, String texture) {
		this.model = model;
		this.texture = Resources.getTexture(texture);
		this.matrix = new Matrix4f();
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
