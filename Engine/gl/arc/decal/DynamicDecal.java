package gl.arc.decal;

import org.joml.Matrix4f;

import core.Resources;
import gl.res.Texture;

public class DynamicDecal {
	private Texture texture;
	private Matrix4f modelMatrix;
	
	public DynamicDecal(String texKey) {
		this.texture = Resources.getTexture(texKey);
		modelMatrix = new Matrix4f();
	}
	
	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	public Texture getTexture() {
		return texture;
	}
}
