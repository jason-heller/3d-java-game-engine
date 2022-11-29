package gl.arc.decal;

import shader.ShaderProgram;
import shader.UniformMat4;
import shader.UniformSampler;

public class DynamicDecalShader extends ShaderProgram {

	protected static final String VERTEX_SHADER = "gl/arc/decal/decal.vert";
	protected static final String FRAGMENT_SHADER = "gl/arc/decal/decal.frag";
	
	public UniformMat4 viewModel = new UniformMat4("viewModel");
	public UniformMat4 projection = new UniformMat4("projection");
	public UniformMat4 invProj = new UniformMat4("invProj");
	public UniformMat4 invViewModel = new UniformMat4("invModelView");
	
	public UniformSampler albedo = new UniformSampler("albedo");
	public UniformSampler depthSamples = new UniformSampler("depthSamples");

	public DynamicDecalShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "vertex");
		super.storeAllUniformLocations(viewModel, projection, invProj, invViewModel, albedo, depthSamples);
		super.bindFragOutput(0, "out_color");
	}
}
