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
	
	public ImageTag(String field, Vector3f offset, int[] viewport, float[] uvOffset) {
		this.setOffset(offset);
		this.setScale(scale);
		this.field = field;
		this.viewport = viewport;
		this.uvOffset = uvOffset;
	}

	@Override
	public void update(Model model, Texture texture, Matrix4f matrix) {
		Matrix4f m = new Matrix4f();
		m.set(matrix);
		m.translate(offset);
		
		Image image = UI.drawImage(field, viewport[0], viewport[1], viewport[2], -viewport[3], m);
		image.setUvOffset(uvOffset[0], uvOffset[1], uvOffset[2], uvOffset[3]);
		image.markAsTemporary();
	}

	@Override
	public void cleanUp() {
	}

}
