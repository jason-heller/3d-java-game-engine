package gl.skybox;

import shader.ShaderProgram;
import shader.UniformMat4;
import shader.UniformSampler;

public class SkyboxShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/skybox/sky.vert";
	private static final String FRAGMENT_SHADER = "gl/skybox/sky.frag";

	protected UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");

	public SkyboxShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position");
		super.storeAllUniformLocations(projectionViewMatrix, sampler);
		//super.bindFragOutput(1, "out_brightness");
	}
}
