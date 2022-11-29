package gl.post;

import org.lwjgl.opengl.GL11;

import gl.Window;
import gl.fbo.FBO;
import shader.UniformSampler;

public class GaussianHBlur extends PostShader {
	private static final String VERTEX_SHADER = "gl/post/glsl/hblur_vertex.glsl";
	private static final String FRAGMENT_SHADER = "gl/post/glsl/gaussian.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");

	public GaussianHBlur() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, Window.getWidth() / 8, Window.getHeight() / 8);
		storeAllUniformLocations(sampler);
	}

	@Override
	public void loadUniforms() {
	}

	public void render(FBO frameBuffer) {
		//bindFbo();

		start();
		loadUniforms();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getColorBuffer());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		stop();

		//unbindFbo();
	}
}