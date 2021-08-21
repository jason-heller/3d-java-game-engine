package gl.skybox._3d;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;
import shader.UniformSampler;

public class Skybox3DShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/skybox/_3D/sky3d.vert";
	private static final String FRAGMENT_SHADER = "gl/skybox/_3D/sky3d.frag";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");
	protected UniformSampler lightmap = new UniformSampler("lightmap");
	public UniformFloat scale = new UniformFloat("scale");

	public Skybox3DShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(projectionMatrix, viewMatrix, modelMatrix, lightmap, sampler, scale);
	}
}
