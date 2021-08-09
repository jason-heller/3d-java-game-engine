package gl.res.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.res.Model;
import gl.res.Texture;
import ui.Text;
import ui.UI;

public class TextTag extends MeshTag {
	
	private String field;
	
	public TextTag(String field, Vector3f offset, float scale) {
		this.setOffset(offset);
		this.setScale(scale);
		this.field = field;
	}

	@Override
	public void update(Model model, Texture texture, Matrix4f matrix) {
		Matrix4f m = new Matrix4f();
		m.set(matrix);
		m.translate(offset);

		String str = MeshData.getField(field);
		if (str == null)
			str = "Missing Value";
		
		Text text = UI.drawString(str, scale, true, m);
		text.markAsTemporary();
	}

	@Override
	public void cleanUp() {
	}

}
