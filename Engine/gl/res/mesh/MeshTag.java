package gl.res.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.res.Model;
import gl.res.Texture;

public abstract class MeshTag {
	protected Vector3f offset;
	protected float scale;
	
	public abstract void update(Model model, Texture texture, Matrix4f matrix);
	public abstract void cleanUp();
	
	public Vector3f getOffset() {
		return offset;
	}
	public void setOffset(Vector3f offset) {
		this.offset = offset;
	}
	public float getScale() {
		return scale;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public abstract float getWidth();
	public abstract float getHeight();
}
