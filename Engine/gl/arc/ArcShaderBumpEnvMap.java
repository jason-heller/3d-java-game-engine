package gl.arc;

import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class ArcShaderBumpEnvMap extends ArcShaderBase {

	private static final String VERTEX_SHADER = "gl/arc/glsl/bump_env.vert";
	private static final String FRAGMENT_SHADER = "gl/arc/glsl/bump_env.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	
	public UniformSampler bumpMap = new UniformSampler("bumpMap");
	public UniformSampler specMap = new UniformSampler("specMap");
	protected UniformSampler envMap = new UniformSampler("envMap");

	public UniformVec3 cameraPos = new UniformVec3("cameraPos");

	public ArcShaderBumpEnvMap() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		addUniforms(bumpMap, specMap, envMap, cameraPos);
	}
}
