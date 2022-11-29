package gl.arc;

import static gl.light.DynamicLightHandler.MAX_DYNAMIC_LIGHTS;

import shader.ShaderProgram;
import shader.UniformMat4;
import shader.UniformMat4Array;
import shader.UniformSampler;
import shader.UniformSamplerArray;
import shader.UniformVec4;

public class ArcHeightmapShader extends ShaderProgram {

	protected static final String VERTEX_SHADER = "gl/arc/glsl/heightmap.vert";
	protected static final String FRAGMENT_SHADER = "gl/arc/glsl/heightmap.frag";

	public UniformMat4 viewMatrix = new UniformMat4("viewMatrix");
	public UniformMat4 projectionMatrix = new UniformMat4("projectionMatrix");
	
	protected UniformSampler sampler1 = new UniformSampler("sampler1");
	protected UniformSampler sampler2 = new UniformSampler("sampler2");
	protected UniformSampler lightmap = new UniformSampler("lightmap");
	
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");
	
	public UniformMat4Array lightSpaceMatrix = new UniformMat4Array("lightSpaceMatrix", MAX_DYNAMIC_LIGHTS);
	public UniformMat4 lightPos = new UniformMat4("lightPos");
	public UniformMat4 lightDir = new UniformMat4("lightDir");
	public UniformVec4 lightInfo = new UniformVec4("lightInfo");
	
	protected UniformSamplerArray shadowMap = new UniformSamplerArray("shadowMap", MAX_DYNAMIC_LIGHTS);

	public ArcHeightmapShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_blends");
		super.storeAllUniformLocations(projectionMatrix, viewMatrix, 
				lightmap, sampler1, sampler2, 
				lightPos, lightDir, lightInfo,
				clipPlane,
				lightSpaceMatrix, shadowMap);
		super.bindFragOutput(0, "out_color");
		//super.bindFragOutput(1, "out_brightness");
	}
}
