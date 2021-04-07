package gl.map.architecture.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Application;
import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.fbo.FrameBuffer;
import gl.water.WaterShader;
import map.architecture.Architecture;
import map.architecture.Material;

public class ArcRender {
	
	private static ArcShader shader;
	private static WaterShader waterShader;
	
	public static void init() {
		shader = new ArcShader();
		waterShader = new WaterShader();
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
	}
	
	public static void startRender(Camera camera, float clipX, float clipY, float clipZ, float clipDist) {
		Matrix4f view = camera.getViewMatrix();
		
		shader.start();
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(view);
		shader.camPos.loadVec3(view.getTranslation());
		shader.clipPlane.loadVec4(clipX, clipY, clipZ, clipDist);
		shader.sampler.loadTexUnit(0);
		shader.lightmap.loadTexUnit(1);
		Resources.getTexture(Debug.fullbright ? "none" : "lightmap").bind(1);
	}
	
	public static void render(Camera camera, Architecture arc, TexturedModel tMesh) {
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		Material material = tMesh.getTexture().getMaterial();
		
		switch(material) {
		case CAMERA:
			arc.callCommand("camview_render index=0");
			renderCamView(tMesh);
			break;
		default:
			renderDefault(tMesh);
		}
		
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	private static void renderDefault(TexturedModel tMesh) {
		(Debug.ambientOnly ? Resources.getTexture("none") : tMesh.getTexture()).bind(0);

		tMesh.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(tMesh.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tMesh.getModel().getVertexCount());
		Render.drawCalls++;
	}
	
	private static void renderCamView(TexturedModel tMesh) {
		FrameBuffer refractionFbo = Render.getRefractionFbo();
		refractionFbo.bindTextureBuffer(0);
		
		tMesh.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(tMesh.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tMesh.getModel().getVertexCount());
		Render.drawCalls++;
	}
	
	public static void finishRender() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	public static void renderWater(Camera camera, Vector3f max, Vector3f min) {
		waterShader.start();
		waterShader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		
		GL11.glEnable(GL11.GL_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		waterShader.setup(camera);

		Vector3f bounds = Vector3f.sub(max, min);
		waterShader.offset.loadVec3(min.x, max.y, min.z);
		waterShader.scales.loadVec2(bounds.x, bounds.z);
		waterShader.timer.loadFloat(Render.getTimer());
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, 12, GL11.GL_UNSIGNED_INT, 0);
		Render.drawCalls++;

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		
		GL11.glDisable(GL11.GL_ALPHA);
		waterShader.stop();
	}
	
	public static void cleanUp() {
		shader.cleanUp();
		waterShader.cleanUp();
	}
}
