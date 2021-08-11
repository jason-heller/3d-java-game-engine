package gl.post;

import org.lwjgl.opengl.GL11;

import gl.Render;
import gl.Window;
import gl.fbo.FboUtils;
import gl.fbo.FrameBuffer;
import shader.ShaderProgram;

public abstract class PostShader extends ShaderProgram {
	protected static final String VERTEX_SHADER = "gl/post/glsl/vertex.glsl";

	private final FrameBuffer fbo;

	public PostShader(String vertexFile, String fragmentFile, int width, int height, String... inVariables) {
		super(vertexFile, fragmentFile, inVariables);
		fbo = FboUtils.createTextureFbo(width, height);
	}

	public PostShader(String vertexFile, String fragmentFile, String... inVariables) {
		super(vertexFile, fragmentFile, inVariables);
		fbo = FboUtils.createTextureFbo(Window.getWidth(), Window.getHeight());
	}

	public void bindFbo() {
		fbo.bind();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		fbo.cleanUp();
	}

	public FrameBuffer getFbo() {
		return fbo;
	}

	public abstract void loadUniforms();
	
	public void render(FrameBuffer frameBuffer) {
		start();
		loadUniforms();

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		frameBuffer.bindTextureBuffer(0);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		Render.drawCalls++;
		stop();
	}

	public void unbindFbo() {
		fbo.unbind();
	}
}
