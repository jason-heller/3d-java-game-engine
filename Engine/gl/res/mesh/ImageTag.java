package gl.res.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.res.Model;
import gl.res.Texture;
import ui.Image;
import ui.UI;

public class ImageTag extends MeshTag {
	
	private String field;
	private int[] viewport;
	private float[] uvOffset;
	private Vector3f color;
	private boolean centered;
	
	public ImageTag(String field, Vector3f offset, int[] viewport, float[] uvOffset, Vector3f color, boolean centered) {
		this.setOffset(offset);
		this.setScale(scale);
		this.field = field;
		this.viewport = viewport;
		this.uvOffset = uvOffset;
		this.color = color;
		this.centered = centered;
	}

	@Override
	public void update(Model model, Texture texture, Matrix4f matrix) {
		Matrix4f m = new Matrix4f();
		m.set(matrix);
		m.translate(offset);
		
		Image image = UI.drawImage(MeshData.getField(field), viewport[0], viewport[1], viewport[2], -viewport[3], m);
		image.setUvOffset(uvOffset[0], uvOffset[1], uvOffset[2], uvOffset[3]);
		image.setColor(color);
		image.setCentered(centered);
		image.markAsTemporary();
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public boolean isCentered() {
		return centered;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}
	
	@Override
	public void cleanUp() {
	}

	public int[] getViewport() {
		return viewport;
	}
	
	public void setViewport(int[] viewport) {
		this.viewport = viewport;
	}

	public float[] getUvOffsets() {
		return uvOffset;
	}

}
