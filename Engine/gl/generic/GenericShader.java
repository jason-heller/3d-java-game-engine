package gl.generic;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3Array;
import shader.UniformVec4;

public class GenericShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/generic/generic.vert";
	private static final String FRAGMENT_SHADER = "gl/generic/generic.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");
	public UniformVec3Array lights = new UniformVec3Array("lights", 6);
	public UniformVec4 color = new UniformVec4("color");

	public GenericShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, modelMatrix, diffuse, lights, color);
	}
}
