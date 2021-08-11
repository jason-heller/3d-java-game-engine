package gl.light;

import shader.ShaderProgram;
import shader.UniformFloatArray;
import shader.UniformMat4Array;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformSamplerArray;
import shader.UniformVec2Array;
import shader.UniformVec3Array;
import shader.UniformVec4;

public class LightShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/light/light.vert";
	private static final String FRAGMENT_SHADER = "gl/light/light.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");
	public UniformVec3Array lights = new UniformVec3Array("lights", 6);
	
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");

	public UniformMat4Array lightSpaceMatrix = new UniformMat4Array("lightSpaceMatrix", 4);
	public UniformVec3Array lightPos = new UniformVec3Array("lightPos", 4);
	public UniformVec3Array lightDir = new UniformVec3Array("lightDir", 4);
	public UniformVec2Array cutoff = new UniformVec2Array("cutoff", 4);
	public UniformFloatArray strength = new UniformFloatArray("strength", 4);

	public UniformSamplerArray shadowMap = new UniformSamplerArray("shadowMap", 4);

	public LightShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, modelMatrix, diffuse, lights, lightPos, lightDir,
				cutoff, strength, clipPlane, lightSpaceMatrix, shadowMap);
	}
}
