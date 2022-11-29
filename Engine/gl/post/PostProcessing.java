package gl.post;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import gl.Camera;
import gl.Render;
import gl.arc.decal.DynamicDecalRender;
import gl.fbo.FBO;
import gl.res.Model;

public class PostProcessing {

	static Map<PostShader, Boolean> shaders = new HashMap<PostShader, Boolean>();

	public static Model quad;

	private static int numActiveShaders = 0;

	public static boolean underwater = false;

	public static final BrightnessShader BRIGHTNESS_SHADER = new BrightnessShader();
	public static final WaveShader WAVE_SHADER = new WaveShader();
	public static final GaussianHBlur H_BLUR_SHADER = new GaussianHBlur();
	public static final GaussianVBlur V_BLUR_SHADER = new GaussianVBlur();
	public static final CombineShader COMBINE_SHADER = new CombineShader();
	public static final NightVisionShader NIGHT_VISION = new NightVisionShader();
	public static final DefaultShader DEFAULT_SHADER = new DefaultShader();

	private static final FBO[] fbos = new FBO[] {Render.screen, Render.screenPong};
	
	private static void addShader(PostShader shader) {
		shaders.put(shader, false);
	}

	public static void cleanUp() {
		quad.cleanUp();

		for (final PostShader shader : shaders.keySet()) {
			shader.cleanUp();
		}

		shaders.clear();

		DynamicDecalRender.cleanUp();
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
		addShader(NIGHT_VISION);

		/*enable(H_BLUR_SHADER);
		enable(V_BLUR_SHADER);
		enable(COMBINE_SHADER);*/
		enable(DEFAULT_SHADER);
		

		DynamicDecalRender.init();
	}

	public static void render(Camera camera) {
		quad.bind(0, 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		int fboId = 1;
		int shaderCount = 0;
		for(PostShader shader : shaders.keySet()) {
			boolean active = shaders.get(shader);
			
			if (active) {
				shaderCount++;
				
				if (shaderCount != numActiveShaders) {
					// Render post
					fbos[fboId].bind();
					fbos[1 - fboId].bindColorBuffer(0);
					shader.render();
					
					fbos[fboId].unbind();
					fboId = 1 - fboId;
				} else {
					fbos[1 - fboId].bindColorBuffer(0);
					shader.render();
					// Render directly to screen
					// FIXME: Decal shader should be treated as a post shader instead of tacked onto
					// the end of the post rendering process
					//DynamicDecalRender.render(camera, fbos[1 - fboId]);	// FIXME: Move DecalRender fully out of Render class and into here

				}
			}
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		quad.unbind(0, 1);
	}

}
