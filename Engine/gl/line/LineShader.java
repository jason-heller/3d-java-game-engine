package gl.line;

import shader.ShaderProgram;
import shader.UniformMatrix;

public class LineShader extends ShaderProgram {
	private static final String VERTEX_SHADER = "gl/line/line.vert";
	private static final String FRAGMENT_SHADER = "gl/line/line.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");

	public LineShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		super.storeAllUniformLocations(projectionViewMatrix);
	}
}
