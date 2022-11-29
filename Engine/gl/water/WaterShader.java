package gl.water;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.fbo.FBO;
import gl.res.Model;
import gl.res.Texture;
import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMat4;
import shader.UniformSampler;
import shader.UniformVec2;
import shader.UniformVec3;

public class WaterShader extends ShaderProgram {
	
	private Texture dudvTexture;
	private Model waterModel;

	private static final String VERTEX_SHADER = "gl/water/water.vert";
	private static final String FRAGMENT_SHADER = "gl/water/water.frag";

	public UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");
	public UniformVec3 offset = new UniformVec3("offset");
	public UniformVec2 scales = new UniformVec2("scales");
	public UniformVec3 cameraPos = new UniformVec3("cameraPos");
	public UniformSampler reflection = new UniformSampler("modelMartix");
	public UniformSampler refraction = new UniformSampler("refraction");
	public UniformSampler dudv = new UniformSampler("dudv");
	public UniformSampler depth = new UniformSampler("depth");
	public UniformFloat timer = new UniformFloat("timer");

	public WaterShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords");
		super.storeAllUniformLocations(projectionViewMatrix, offset, scales, cameraPos, reflection, refraction, dudv, depth, timer);
		dudvTexture = Resources.addTexture("dudv", "water/dudv.png");
		waterModel = Resources.addObjModel("water", "water/water.obj");
	}

	public void setup(Camera camera) {
		waterModel.bind(0, 1);
		reflection.loadTexUnit(0);
		refraction.loadTexUnit(1);
		dudv.loadTexUnit(2);
		depth.loadTexUnit(3);
		cameraPos.loadVec3(camera.getPosition());
		Render.getReflectionFbo().bindColorBuffer(0);
		FBO refractionFbo = Render.getRefractionFbo();
		refractionFbo.bindColorBuffer(1);
		
		dudvTexture.bind(2);
		
		refractionFbo.bindDepthBuffer(3);
		Render.textureSwaps++;
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		dudvTexture.delete();
		waterModel.cleanUp();
	}
}
