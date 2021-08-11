package gl.post;

import org.lwjgl.opengl.GL11;

import dev.Console;
import gl.Render;
import gl.fbo.FrameBuffer;

public class DefaultShader extends PostShader {
	protected static final String VERTEX_SHADER = "gl/post/glsl/vertex.glsl";
	protected static final String FRAGMENT_SHADER = "gl/post/glsl/fragment.glsl";

	public DefaultShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
	}

	@Override
	public void loadUniforms() {
	}
}
