package gl.anim.render;

import java.util.concurrent.CopyOnWriteArrayList;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import gl.Camera;
import gl.Render;
import gl.anim.Animator;
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.Architecture;
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
			if (entity.deactivated || !entity.visible) continue;
			
			Model model = entity.getModel();
			final Animator animator = entity.getAnimator();
			
			int numMeshes = model.getMeshes().length;
			
			Matrix3f invTransRotMatrix = new Matrix3f();
			invTransRotMatrix.rotateX(entity.rot.x);
			invTransRotMatrix.rotateY(entity.rot.y);
			invTransRotMatrix.rotateZ(entity.rot.z);
			
			
			for(int i = 0; i < numMeshes; i++) {
				Mesh mesh = model.getMeshes()[i];
				Texture texture = model.getTextures()[i];
				
				texture.bind(0);
				shader.specularity.loadFloat(0f);

				shader.diffuse.loadTexUnit(0);
				shader.specular.loadTexUnit(1);
				shader.lights.loadVec3(entity.getLighting());

				mesh.bind(0, 1, 2, 3, 4);
				shader.modelMatrix.loadMatrix(entity.getMatrix());
				shader.invTransRotMatrix.loadMatrix(invTransRotMatrix);
				shader.jointTransforms.loadMatrixArray(animator.getJointTransforms());
				
				GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				Render.drawCalls++;
				mesh.unbind(0, 1, 2, 3, 4);
			}
		}
		shader.stop();
	}

}
