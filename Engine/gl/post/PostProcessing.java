package gl.post;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import gl.Render;
import gl.res.Model;

public class PostProcessing {

	static Map<PostShader, Boolean> shaders = new HashMap<PostShader, Boolean>();

	private static Model quad;

	private static int numActiveShaders = 0;

	public static boolean underwater = false;

	public static final BrightnessShader BRIGHTNESS_SHADER = new BrightnessShader();
	public static final WaveShader WAVE_SHADER = new WaveShader();
	public static final GaussianHBlur H_BLUR_SHADER = new GaussianHBlur();
	public static final GaussianVBlur V_BLUR_SHADER = new GaussianVBlur();
	public static final CombineShader COMBINE_SHADER = new CombineShader();
	public static final DefaultShader DEFAULT_SHADER = new DefaultShader();

	private static void addShader(PostShader shader) {
		shaders.put(shader, false);
	}

	public static void cleanUp() {
		quad.cleanUp();

		for (final PostShader shader : shaders.keySet()) {
			shader.cleanUp();
		}

		shaders.clear();
	}

	public static void disable(PostShader shader) {
		if (shaders.get(shader)) {
			numActiveShaders--;
		}
		shaders.put(shader, false);
	}

	public static void enable(PostShader shader) {
		if (!shaders.get(shader)) {
			numActiveShaders++;
		}
		shaders.put(shader, true);
	}

	public static int getNumActiveShaders() {
		return numActiveShaders;
	}

	public static void init() {
		quad = Model.create();
		quad.bind();
		quad.createAttribute(0, new float[] { -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f }, 2);
		quad.createAttribute(1, new float[] { 0, 0, 1, 0, 0, 1, 1, 1 }, 2);
		quad.unbind();

		// Added later = higher priority
		// addShader(BRIGHTNESS_SHADER);
		addShader(WAVE_SHADER);
		addShader(H_BLUR_SHADER);
		addShader(V_BLUR_SHADER);
		addShader(COMBINE_SHADER);
		addShader(DEFAULT_SHADER);

		enable(H_BLUR_SHADER);
		enable(V_BLUR_SHADER);
		enable(COMBINE_SHADER);
		enable(DEFAULT_SHADER);
	}

	public static void render() {

		quad.bind(0, 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		if (PostProcessing.underwater ) {
			WAVE_SHADER.render(Render.screen);
		} else {
			DEFAULT_SHADER.render(Render.screen);
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		quad.unbind(0, 1);
	}

}
