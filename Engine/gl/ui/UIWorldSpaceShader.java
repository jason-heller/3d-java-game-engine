package gl.ui;

import shader.ShaderProgram;
import shader.UniformBoolean;
import shader.UniformFloat;
import shader.UniformMat4;
import shader.UniformVec3;
import shader.UniformVec4;

public class UIWorldSpaceShader extends ShaderProgram {

	private static final String VERTEX_FILE = "gl/ui/ui_ws.vert";
	private static final String FRAGMENT_FILE = "gl/ui/ui.frag";

	public UniformVec4 translation = new UniformVec4("translation");
	public UniformVec4 offset = new UniformVec4("offset");
	public UniformVec3 color = new UniformVec3("color");
	public UniformBoolean centered = new UniformBoolean("centered");
	public UniformFloat opacity = new UniformFloat("opacity");
	public UniformFloat rotation = new UniformFloat("rot");
	public UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");
	public UniformMat4 worldMatrix = new UniformMat4("worldMatrix");

	public UIWorldSpaceShader() {
		super(VERTEX_FILE, FRAGMENT_FILE, "in_vertices", "in_uvs");
		super.storeAllUniformLocations(color, translation, offset, centered, opacity, rotation, projectionViewMatrix, worldMatrix);
	}

}
