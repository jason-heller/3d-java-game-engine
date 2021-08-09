package gl.terrain;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec2;
import shader.UniformVec3;

public class TerrainShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/terrain/terra.vert";
	private static final String FRAGMENT_SHADER = "gl/terrain/terra.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformVec2 offset = new UniformVec2("offset");
	
	public TerrainShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, diffuse, lightDirection, offset);
	}
}
