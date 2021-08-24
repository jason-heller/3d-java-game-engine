package gl.arc;

import shader.UniformSampler;
import shader.UniformVec3;

public class ArcShaderEnvMap extends ArcShaderBase {

	private static final String VERTEX_SHADER = "gl/arc/glsl/env.vert";
	private static final String FRAGMENT_SHADER = "gl/arc/glsl/env.frag";

	public UniformSampler specMap = new UniformSampler("specMap");
	protected UniformSampler envMap = new UniformSampler("envMap");
	public UniformVec3 cameraPos = new UniformVec3("cameraPos");

	public ArcShaderEnvMap() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		addUniforms(specMap, envMap, cameraPos);
	}
}
