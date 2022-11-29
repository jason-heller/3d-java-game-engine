package gl.arc;

import shader.ShaderProgram;
import shader.UniformMat4;
import shader.UniformSampler;
import shader.UniformVec4;

public class ArcShaderCheap extends ShaderProgram {

	protected static final String VERTEX_SHADER = "gl/arc/glsl/cheap.vert";
	protected static final String FRAGMENT_SHADER = "gl/arc/glsl/cheap.frag";

	public UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");

	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformSampler lightmap = new UniformSampler("lightmap");

	public UniformVec4 clipPlane = new UniformVec4("clipPlane");

	public ArcShaderCheap() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, lightmap, sampler, clipPlane);
		super.bindFragOutput(0, "outputColor");
	}
}
