package gl.post;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import gl.fbo.FBO;
import shader.UniformSampler;

public class CombineShader extends PostShader {
	protected static final String FRAGMENT_SHADER = "gl/post/glsl/combine.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");
	protected UniformSampler hlSampler = new UniformSampler("highlightSampler");

	public CombineShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		storeAllUniformLocations(sampler, hlSampler);
	}

	@Override
	public void loadUniforms() {
		sampler.loadTexUnit(0);
		hlSampler.loadTexUnit(1);
	}

	public void render(FBO color, FBO highlight) {
		// bindFbo();

		start();
		loadUniforms();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, color.getColorBuffer());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, highlight.getColorBuffer());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		stop();

		// unbindFbo();

		// FboUtils.resolve(this.getFbo());
	}
}