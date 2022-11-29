package gl.post;

import org.lwjgl.opengl.GL11;

import gl.Render;
import shader.ShaderProgram;

public abstract class PostShader extends ShaderProgram {
	protected static final String VERTEX_SHADER = "gl/post/glsl/vertex.glsl";

	public PostShader(String vertexFile, String fragmentFile, int width, int height, String... inVariables) {
		super(vertexFile, fragmentFile, inVariables);
		super.bindFragOutput(0, "out_color");
	}

	public PostShader(String vertexFile, String fragmentFile, String... inVariables) {
		super(vertexFile, fragmentFile, inVariables);
		super.bindFragOutput(0, "out_color");
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	public abstract void loadUniforms();
	
	public void render() {
		start();
		loadUniforms();

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		Render.drawCalls++;
		stop();
	}
}
