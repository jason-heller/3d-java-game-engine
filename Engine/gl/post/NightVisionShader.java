package gl.post;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import core.Resources;
import gl.Render;
import gl.fbo.FrameBuffer;
import shader.UniformFloat;
import shader.UniformSampler;

public class NightVisionShader extends PostShader {
	private static final String FRAGMENT_SHADER = "gl/post/glsl/nightvis.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");
	protected UniformSampler noiseSampler = new UniformSampler("noiseSampler");
	public UniformFloat timer = new UniformFloat("timer");
	public UniformFloat noiseAmplifierUniform = new UniformFloat("noiseAmplifier");
	
	public static float noiseAmplifier = 1f;

	public NightVisionShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		storeAllUniformLocations(sampler, noiseSampler, timer, noiseAmplifierUniform);
	}

	@Override
	public void loadUniforms() {
		this.timer.loadFloat(Render.getTimer());
		this.sampler.loadTexUnit(0);
		this.noiseSampler.loadTexUnit(1);
		this.noiseAmplifierUniform.loadFloat(noiseAmplifier);
	}

	@Override
	public void render(FrameBuffer frameBuffer) {
		start();
		loadUniforms();

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getTextureBuffer());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		Resources.getTexture("noise").bind(1);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		Render.drawCalls++;
		stop();
	}
}