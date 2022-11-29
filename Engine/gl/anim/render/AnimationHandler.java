package gl.anim.render;

import java.util.concurrent.CopyOnWriteArrayList;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import dev.cmd.Console;
import gl.Camera;
import gl.Render;
import gl.anim.Animator;
import scene.Scene;
import scene.entity.Entity;

// TODO: Rework shaders & animation code
public class AnimationHandler {

	private static AnimationShader shader;
	private static CopyOnWriteArrayList<Entity> entityBatch;

	public static void add(Entity e) {
		entityBatch.add(e);
	}

	public static void cleanUp() {
		shader.cleanUp();
	}

	public static void init() {
		shader = new AnimationShader();
		entityBatch = new CopyOnWriteArrayList<Entity>();
	}

	public static void remove(Entity e) {
		entityBatch.remove(e);
	}

	public static void render(Camera camera, Scene scene) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.cameraPos.loadVec3(camera.getPosition());

		for (final Entity entity : entityBatch) {
			if (entity.deactivated) continue;
			
			final Animator animator = entity.getAnimator();
			
			// animator.update();
			entity.getTexture().bind(0);
			shader.specularity.loadFloat(0f);

			shader.diffuse.loadTexUnit(0);
			shader.specular.loadTexUnit(1);

			entity.getModel().bind(0, 1, 2, 3, 4);
			shader.modelMatrix.loadMatrix(entity.getMatrix());
			shader.jointTransforms.loadMatrixArray(animator.getJointTransforms());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			Render.drawCalls++;
			entity.getModel().unbind(0, 1, 2, 3, 4);
		}
		shader.stop();
	}

	public static void renderViewmodel(Scene scene, Entity animatedModel) {
		final Camera camera = scene.getCamera();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.cameraPos.loadVec3(camera.getPosition());

		animatedModel.getTexture().bind(0);
		animatedModel.getModel().bind(0, 1, 2, 3, 4);
		animatedModel.getTexture().bind(0);
		shader.specularity.loadFloat(0f);

		shader.diffuse.loadTexUnit(0);
		shader.specular.loadTexUnit(1);
		shader.modelMatrix.loadMatrix(animatedModel.getMatrix());
		shader.jointTransforms.loadMatrixArray(animatedModel.getAnimator().getJointTransforms());
		GL11.glDrawElements(GL11.GL_TRIANGLES, animatedModel.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		animatedModel.getModel().unbind(0, 1, 2, 3, 4);

		shader.stop();
	}

}
