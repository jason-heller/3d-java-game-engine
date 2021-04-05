package gl.water;

import core.Resources;
import dev.Console;
import gl.Camera;
import gl.Render;
import gl.post.PostProcessing;
import gl.res.Model;
import gl.res.Texture;
import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec2;
import shader.UniformVec3;

public class WaterShader extends ShaderProgram {
	
	private Texture waterTexture, dudvTexture;
	private Model waterModel;

	private static final String VERTEX_SHADER = "gl/water/water.vert";
	private static final String FRAGMENT_SHADER = "gl/water/water.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformVec3 offset = new UniformVec3("offset");
	public UniformVec2 scales = new UniformVec2("scales");
	public UniformSampler reflection = new UniformSampler("modelMartix");
	public UniformSampler refraction = new UniformSampler("refraction");
	public UniformSampler dudv = new UniformSampler("dudv");
	public UniformFloat timer = new UniformFloat("timer");

	public WaterShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords");
		super.storeAllUniformLocations(projectionViewMatrix, offset, scales, reflection, refraction, dudv, timer);
		waterTexture = Resources.addTexture("water", "water/water.png");
		dudvTexture = Resources.addTexture("dudv", "water/dudv.png");
		waterModel = Resources.addObjModel("water", "water/water.obj");
		
	}

	public void loadModelAndTextures() {
		waterModel.bind(0, 1);
		
		reflection.loadTexUnit(0);
		refraction.loadTexUnit(1);
		dudv.loadTexUnit(2);
		
		Render.getReflectionFbo().bindTextureBuffer(0);
		Render.getRefractionFbo().bindTextureBuffer(1);
		
		dudvTexture.bind(2);
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		waterTexture.delete();
		dudvTexture.delete();
		waterModel.cleanUp();
	}
}
