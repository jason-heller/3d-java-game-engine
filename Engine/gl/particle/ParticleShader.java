package gl.particle;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMat4;

public class ParticleShader extends ShaderProgram {

	protected static final String VERTEX_SHADER = "gl/particle/particle.vert";
	protected static final String FRAGMENT_SHADER = "gl/particle/particle.frag";

	protected UniformMat4 projectionMatrix = new UniformMat4("projectionMatrix");
	protected UniformFloat numRows = new UniformFloat("numRows");

	public ParticleShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		super.storeAllUniformLocations(projectionMatrix, numRows);
	}

}
