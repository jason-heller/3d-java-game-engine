package gl.arc;

import static gl.light.DynamicLightHandler.MAX_DYNAMIC_LIGHTS;

import shader.ShaderProgram;
import shader.Uniform;
import shader.UniformMat3;
import shader.UniformMat4;
import shader.UniformMat4Array;
import shader.UniformSampler;
import shader.UniformSamplerArray;
import shader.UniformVec4;

public class ArcShaderBase extends ShaderProgram {

	protected static final String VERTEX_SHADER = "gl/arc/glsl/base.vert";
	protected static final String FRAGMENT_SHADER = "gl/arc/glsl/base.frag";

	public UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");
	
	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformSampler lightmap = new UniformSampler("lightmap");
	
	public UniformMat3 lightStyles = new UniformMat3("lightStyles");
	
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");
	
	public UniformMat4Array lightSpaceMatrix = new UniformMat4Array("lightSpaceMatrix", MAX_DYNAMIC_LIGHTS);
	public UniformMat4 lightPos = new UniformMat4("lightPos");
	public UniformMat4 lightDir = new UniformMat4("lightDir");
	public UniformVec4 lightInfo = new UniformVec4("lightInfo");
	
	protected UniformSamplerArray shadowMap = new UniformSamplerArray("shadowMap", MAX_DYNAMIC_LIGHTS);

	public ArcShaderBase() {
		this(VERTEX_SHADER, FRAGMENT_SHADER);
		addUniforms();
	}
	
	public ArcShaderBase(String vert, String frag) {
		super(vert, frag, "in_position", "in_textureCoords", "in_normals", "in_tangents");
		super.bindFragOutput(0, "outputColor");
	}
	
	public void addUniforms(Uniform ...extraUniforms) {
		Uniform[] uniforms = new Uniform[10 + extraUniforms.length];
		uniforms[0] = projectionViewMatrix;
		uniforms[1] = lightmap;
		uniforms[2] = sampler;
		uniforms[3] = lightPos;
		uniforms[4] = lightDir;
		uniforms[5] = lightInfo;
		uniforms[6] = clipPlane;
		uniforms[7] = lightSpaceMatrix;
		uniforms[8] = shadowMap;
		uniforms[9] = lightStyles;
		
		for(int i = 0; i < extraUniforms.length; i++) {
			uniforms[i + 10] = extraUniforms[i];
		}
		
		super.storeAllUniformLocations(uniforms);
	}
}
