package gl.line;

import shader.ShaderProgram;
import shader.UniformMat4;

public class LineShader extends ShaderProgram {
	protected static final String VERTEX_SHADER = "gl/line/line.vert";
	protected static final String FRAGMENT_SHADER = "gl/line/line.frag";

	public UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");

	public LineShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		super.storeAllUniformLocations(projectionViewMatrix);
	}
}
