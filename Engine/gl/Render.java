package gl;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import core.Application;
import core.Resources;
import gl.entity.GenericShader;
import gl.fbo.FrameBuffer;
import gl.line.LineRender;
import gl.particle.ParticleHandler;
import gl.post.PostProcessing;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
import scene.Scene;
import ui.UI;

public class Render {
	//private static FrameBuffer screenMultisampled;
	public static FrameBuffer screen;
	private static GenericShader genericShader;
	
	public static void cleanUp() {
		genericShader.cleanUp();
		LineRender.cleanUp();
		Resources.cleanUp();
		//EntityControl.cleanUp();
		UI.cleanUp();
		ParticleHandler.cleanUp();
		PostProcessing.cleanUp();
		//screenMultisampled.cleanUp();
	}

	public static void init() {
		//EntityControl.init();
		UI.init();
		ParticleHandler.init();
		LineRender.init();

		screen = new FrameBuffer(1280, 720, true, true, false, false, 1);
		//screenMultisampled = new FrameBuffer(1280, 720, true, true, false, false, 1);
		
		PostProcessing.init();

		Resources.addTexture("skybox", "default.png");
		Resources.addTexture("default", "default.png");
		Resources.addTexture("none", "flat.png");
		Resources.addObjModel("cube", "cube.obj", true);
		Resources.addSound("click", "lighter_click.ogg");

		initGuiTextures();
		
		genericShader = new GenericShader();
	}

	private static void initGuiTextures() {
		Resources.addTexture("gui_slider", "gui/slider.png");
		Resources.addTexture("gui_arrow", "gui/arrow.png");
	}

	public static void postRender(Scene scene) {
	screen.unbind();
		//FboUtils.resolve(screen);
		if (PostProcessing.getNumActiveShaders() != 0) {
			PostProcessing.render();
		}
		UI.render(scene);
	}

	/** The main render method for the engine, should only be called once per render pass.
	 */
	public static void renderPass(Scene scene) {
		Camera camera = scene.getCamera();
		ParticleHandler.update(camera);
		screen.bind();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		scene.render();
		//if (scene instanceof Overworld) {
			//Enviroment e = ow.getEnviroment();
			//waterRender.render(camera, e);
			
			ParticleHandler.render(camera);
		//}
			LineRender.render(camera);
	}
	
	/** Does a singular render pass for one textured model, in worldspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 * @param texMesh the textured model to draw.
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderModel(TexturedModel texMesh, Vector3f lightDirection) {
		renderModel(texMesh.getModel(), texMesh.getTexture(), texMesh.getMatrix(), lightDirection);
	}
	
	/** Does a singular render pass for one textured model, in worldspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 * @param model the mesh to draw
	 * @param texture the texture to apply to the mesh
	 * @param matrix the model matrix for the mesh
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderModel(Model model, Texture texture, Matrix4f matrix, Vector3f lightDirection) {
		Camera camera = Application.scene.getCamera();
		genericShader.start();
		genericShader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		genericShader.lightDirection.loadVec3(lightDirection);
		
		texture.bind(0);
		genericShader.modelMatrix.loadMatrix(matrix);
		model.bind(0, 1, 2);
		genericShader.color.loadVec4(1, 1, 1, 1);
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		
		genericShader.stop();
	}
	
	/** Does a singular render pass for one textured model, in viewspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 *  Not to mention, you'll make the viewport crowded... 
	 * @param texMesh the textured model to draw.
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderViewModel(TexturedModel texMesh, Vector3f lightDir) {
		renderViewModel(texMesh.getModel(), texMesh.getTexture(), texMesh.getMatrix(), lightDir);
	}
	
	/** Does a singular render pass for one textured model, in viewspace.<br><br>
	 *  Should not be used often as this is a relatively expensive call compared to batching the meshes into a single shader run.
	 *  Not to mention, you'll make the viewport crowded...
	 * @param model the mesh to draw
	 * @param texture the texture to apply to the mesh
	 * @param matrix the model matrix for the mesh
	 * @param lightDirection a vector representing the direction of ambient light in the scene
	 */
	public static void renderViewModel(Model model, Texture texture, Matrix4f matrix, Vector3f lightDir) {
		Camera camera = Application.scene.getCamera();
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		genericShader.start();
		genericShader.projectionViewMatrix.loadMatrix(camera.getProjectionMatrix());
		
		genericShader.lightDirection.loadVec3(lightDir);
		genericShader.color.loadVec4(1, 1, 1, 1);
		
		texture.bind(0);
		genericShader.modelMatrix.loadMatrix(matrix);
		model.bind(0, 1, 2);
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		
		genericShader.stop();
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	public static GenericShader getGenericShader() {
		return genericShader;
	}
}
