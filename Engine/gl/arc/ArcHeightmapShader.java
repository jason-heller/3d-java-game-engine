package gl.arc;

import shader.ShaderProgram;
import shader.UniformFloatArray;
import shader.UniformMat4Array;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformSamplerArray;
import shader.UniformVec2Array;
import shader.UniformVec3Array;
import shader.UniformVec4;

public class ArcHeightmapShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/arc/heightmap.vert";
	private static final String FRAGMENT_SHADER = "gl/arc/heightmap.frag";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	
	protected UniformSampler sampler1 = new UniformSampler("sampler1");
	protected UniformSampler sampler2 = new UniformSampler("sampler2");
	protected UniformSampler lightmap = new UniformSampler("lightmap");
	
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");
	
	public UniformMat4Array lightSpaceMatrix = new UniformMat4Array("lightSpaceMatrix", 4);
	public UniformVec3Array lightPos = new UniformVec3Array("lightPos", 4);
	public UniformVec3Array lightDir = new UniformVec3Array("lightDir", 4);
	public UniformVec2Array cutoff = new UniformVec2Array("cutoff", 4);
	public UniformFloatArray strength = new UniformFloatArray("strength", 4);
	
	protected UniformSamplerArray shadowMap = new UniformSamplerArray("shadowMap", 4);

	public ArcHeightmapShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_blends");
		super.storeAllUniformLocations(projectionMatrix, viewMatrix, 
				lightmap, sampler1, sampler2, 
				lightPos, lightDir, cutoff, strength,
				clipPlane,
				lightSpaceMatrix, shadowMap);
		super.bindFragOutput(0, "out_color");
		super.bindFragOutput(1, "out_brightness");
	}
}
