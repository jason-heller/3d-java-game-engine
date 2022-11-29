package gl.fbo;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import dev.cmd.Console;

public class FBO {

	private int width, height;

	private int id;

	private int color, depth;

	public FBO(int width, int height) {
		this(width, height, true, true);
	}
	
	public FBO(int width, int height, boolean hasColor, boolean hasDepth) {
		this.width = width;
		this.height = height;

		id = GL30.glGenFramebuffers();
		bind();
		
		if (hasColor)
			color = attachTexture(GL11.GL_RGB, GL11.GL_FLOAT, GL30.GL_COLOR_ATTACHMENT0);
		if (hasDepth)
			depth = attachTexture(GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, GL30.GL_DEPTH_ATTACHMENT);

		unbind();
	}

	private int attachTexture(int format, int type, int attachment) {
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, width, height, 0, format, type, (ByteBuffer) null);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, texture, 0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			Console.severe("Failed to bind attachment to fbo");

		return texture;
	}

	public void bind() {
		GL11.glViewport(0, 0, width, height);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
	}

	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}

	public void bindColorBuffer(int location) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + location);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, color);
	}

	public void bindDepthBuffer(int location) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + location);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depth);
	}

	public void cleanUp() {
		GL30.glDeleteFramebuffers(id);
	}

	public int getColorBuffer() {
		return color;
	}

	public int getDepthBuffer() {
		return depth;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void resize(int newWidth, int newHeight) {
		/*if (newWidth == width && newHeight == height)
			return;
		unbind();
		width = newWidth;
		height = newHeight;
		
		if (color != -1) {
			GL11.glDeleteTextures(color);
			color = attachTexture(GL11.GL_RGB, GL11.GL_FLOAT, GL30.GL_COLOR_ATTACHMENT0);
		}
		if (depth != -1) {
			GL11.glDeleteTextures(depth);
			depth = attachTexture(GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, GL30.GL_DEPTH_ATTACHMENT);
		}
*/
	}
}
