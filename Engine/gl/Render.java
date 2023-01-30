package gl;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import core.App;
import core.Resources;
import dev.Debug;
import gl.fbo.FBO;
import gl.generic.GenericShader;
import gl.generic.LightGenericShader;
import gl.line.LineRender;
import gl.particle.ParticleHandler;
import gl.post.PostProcessing;
import gl.res.Mesh;
import gl.res.Texture;
import gl.res.TexturedModel;
import map.architecture.functions.commands.CamView;
import scene.Scene;
import ui.UI;
import util.Matrices;

public class Render {
	//private static FrameBuffer screenMultisampled;
	public static FBO screen, screenPong;
	private static GenericShader genericShader;
	private static LightGenericShader lightShader;
	
	public static int shadowQuality = 1;
	public static float defaultBias = -1f;
	
	public static int textureSwaps = 0, drawCalls = 0;
	
	private static FBO reflection, refraction;
	private static float lastWaterFboPos = Float.POSITIVE_INFINITY;
	private static float timer = 0f;
	
	public static int waterQuality = 4;		// TODO: Make the resizing of this not fuck everything up
	public static float scale = 1f;
	
	public static void cleanUp() {
		genericShader.cleanUp();
		lightShader.cleanUp();
		LineRender.cleanUp();
		Resources.cleanUp();
		//EntityControl.cleanUp();
		UI.cleanUp();
		ParticleHandler.cleanUp();
		PostProcessing.cleanUp();
		//screenMultisampled.cleanUp();
		screen.cleanUp();
		screenPong.cleanUp();
		reflection.cleanUp();
		refraction.cleanUp();
	}

	public static void init() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		// Assets
		final int width = (int) (Window.getWidth() * scale);
		final int height = (int) (Window.getHeight() * scale);
		final int widthQtr = width / 4;
		final int heightQtr = height / 4;
		screen 		= new FBO(width, height);
		screenPong 	= new FBO(width, height);
		reflection 	= new FBO(widthQtr * waterQuality, heightQtr * waterQuality, true, true);
		refraction 	= new FBO(widthQtr * waterQuality, heightQtr * waterQuality, true, true);
		
		
		Resources.initBaseResources();
		
		Resources.addTexture("screen", screen.getColorBuffer(), screen.getWidth(), screen.getHeight());
		Resources.addTexture("reflection", reflection.getColorBuffer(), reflection.getWidth(), reflection.getHeight());

		initGuiTextures();
		
		// Renderers
		UI.init();
		ParticleHandler.init();
		LineRender.init();
		
		PostProcessing.init();
		
		genericShader = new GenericShader();
		lightShader = new LightGenericShader();
	}

	private static void initGuiTextures() {
		Resources.addTexture("gui_slider", "gui/slider.png");
		Resources.addTexture("gui_arrow", "gui/arrow.png");
	}
	
	public static void renderWaterFbos(Scene scene, Camera camera, float waterPlaneY) {
		if (lastWaterFboPos != waterPlaneY) {
			lastWaterFboPos = waterPlaneY;		// HACK: Prevents the reflection/refraction FBOs from calculating the same thing multiple times
			screen.unbind();
			renderRefractions(scene, camera, waterPlaneY);
			renderReflections(scene, camera, waterPlaneY);
			screen.bind();
		}
	}

	/** The main render method for the engine, should only be called once per render pass.
	 */
	public static void renderPass(Scene scene) {
		Camera camera = scene.getCamera();
		textureSwaps = 0;
		drawCalls = 0;
		
		ParticleHandler.update(camera);
		
		if (CamView.requestRender) {
			Render.renderCamView(camera, scene, CamView.renderPos, CamView.renderRot);

			CamView.requestRender = false;
		}

		screen.bind();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		
		scene.render(new Vector4f(0, 1, 0, Float.POSITIVE_INFINITY));
		scene.renderNoReflect();
		
		ParticleHandler.render(camera);
		LineRender.render(camera);
		
		screen.unbind();

		PostProcessing.render(camera);
		
		scene.postRender();
		
		UI.render(scene);
		
		timer += Window.deltaTime;
		
		lastWaterFboPos = Float.POSITIVE_INFINITY;
	}
	
	/** Does a singular render pass for one textured model, in worldspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 * @param texMesh the textured model to draw.
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderModel(TexturedModel texMesh, Vector3f[] lights) {
		renderModel(texMesh.getModel(), texMesh.getTexture(), texMesh.getMatrix(), lights);
	}
	
	/** Does a singular render pass for one textured model, in worldspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 * @param model the mesh to draw
	 * @param texture the texture to apply to the mesh
	 * @param matrix the model matrix for the mesh
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderModel(Mesh model, Texture texture, Matrix4f matrix, Vector3f[] lights) {
		Camera camera = App.scene.getCamera();
		genericShader.start();
		genericShader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		genericShader.lights.loadVec3(lights);
		
		texture.bind(0);
		genericShader.modelMatrix.loadMatrix(matrix);
		model.bind(0, 1, 2);
		genericShader.color.loadVec4(1, 1, 1, 1);
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		drawCalls++;
		
		genericShader.stop();
	}
	
	/** Does a singular render pass for one textured model, in viewspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 *  Not to mention, you'll make the viewport crowded... 
	 * @param texMesh the textured model to draw.
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderViewModel(TexturedModel texMesh, Vector3f[] lights) {
		renderViewModel(texMesh.getModel(), texMesh.getTexture(), texMesh.getMatrix(), lights);
	}
	
	/** Does a singular render pass for one textured model, in viewspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 *  Not to mention, you'll make the viewport crowded...
	 * @param model the mesh to draw
	 * @param texture the texture to apply to the mesh
	 * @param matrix the model matrix for the mesh
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderViewModel(Mesh model, Texture texture, Matrix4f matrix, Vector3f[] lights) {
		Camera camera = App.scene.getCamera();
		genericShader.start();
		genericShader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		
		genericShader.lights.loadVec3(lights);
		genericShader.color.loadVec4(1, 1, 1, 1);
		
		
		if (Debug.ambientOnly) {
			Resources.NO_TEXTURE.bind(0);
		} else {
			texture.bind(0);
		}
		
		genericShader.modelMatrix.loadMatrix(Matrices.mul(camera.getViewModelMatrix(), matrix));
		model.bind(0, 1, 2);
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		drawCalls++;
		genericShader.stop();
	}
	
	private static void renderRefractions(Scene scene, Camera camera, float clipDist) {
		refraction.bind();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		scene.fastRender(new Vector4f(0, -1, 0, clipDist));
		
		refraction.unbind();
	}

	private static void renderReflections(Scene scene, Camera camera, float clipDist) {
		float offset = (camera.getPosition().y - clipDist)*2;
		
		float pitch = camera.getPitch();
		float yaw = camera.getYaw();
		float roll = camera.getRoll();
		reflection.bind();
		camera.setPitch(-camera.getEffectedPitch());
		camera.setYaw(camera.getEffectedYaw());		// TODO: Broken
		camera.setRoll(-camera.getEffectedRoll());
		camera.getPosition().y -= offset;
		camera.updateViewMatrixRaw();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		scene.fastRender(new Vector4f(0, 1, 0, -clipDist));
		
		reflection.unbind();
		camera.setPitch(pitch);
		camera.setYaw(yaw);
		camera.setRoll(roll);
		camera.getPosition().y += offset;
		camera.updateViewMatrix();
	}
	
	public static FBO getReflectionFbo() {
		return reflection;
	}
	
	public static FBO getRefractionFbo() {
		return refraction;
	}
	
	public static GenericShader getGenericShader() {
		return genericShader;
	}
	
	public static LightGenericShader getLightShader() {
		return lightShader;
	}

	public static float getTimer() {
		return timer;
	}
	
	public static void setWaterQuality(int quality) {
		waterQuality = quality;
		// TODO: change fbos
	}

	private static void renderCamView(Camera camera, Scene scene, Vector3f pos, Vector3f rot) {
		final Vector3f origPos = new Vector3f(camera.getPosition());
		final float yaw = camera.getYaw();
		final float pitch = camera.getPitch();
		final float roll = camera.getRoll();

		refraction.bind();
		camera.setYaw(rot.x);
		camera.setPitch(rot.y);
		camera.setRoll(rot.z);
		camera.getPosition().set(pos);
		camera.updateViewMatrix();

		scene.render(new Vector4f(0, 1, 0, Float.POSITIVE_INFINITY));
		
		refraction.unbind();
		camera.setPitch(pitch);
		camera.setYaw(yaw);
		camera.setRoll(roll);
		camera.getPosition().set(origPos);
		camera.updateViewMatrix();
	}
}
