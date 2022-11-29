package gl.arc;

import shader.UniformSampler;

public class ArcShaderBump extends ArcShaderBase {

	protected static final String VERTEX_SHADER = "gl/arc/glsl/bump.vert";
	protected static final String FRAGMENT_SHADER = "gl/arc/glsl/bump.frag";

	public UniformSampler bumpMap = new UniformSampler("bumpMap");

	public ArcShaderBump() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		addUniforms(bumpMap);
	}
}
