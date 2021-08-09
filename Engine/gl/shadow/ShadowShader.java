package gl.shadow;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformSamplerArray;
import shader.UniformVec2Array;
import shader.UniformVec3Array;
import shader.UniformVec4;

public class ShadowShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/shadow/shadow.vert";
	private static final String FRAGMENT_SHADER = "gl/shadow/shadow.frag";

	public UniformMatrix lightSpaceMatrix = new UniformMatrix("lightSpaceMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");

	public ShadowShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "position");
		super.storeAllUniformLocations(lightSpaceMatrix, modelMatrix);
		super.bindFragOutput(0, "out_color");
	}
}
