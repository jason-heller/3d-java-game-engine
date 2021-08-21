package gl.arc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.fbo.FrameBuffer;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.Material;
import map.architecture.vis.Cluster;

public class ArcRenderLightGeneric {
	
	private static ArcShader arcShader;
	
	private static Texture lastTexture = null;
	
	public static boolean fullRender = false;
	
	public static void init() {
		arcShader = new ArcShader();
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
	}
	
	public static void startRender(Camera camera, Vector4f clipPlane, boolean hasLightmap, Matrix4f lightProjMatrix, DynamicLight[] lights) {
		Matrix4f view = camera.getViewMatrix();

		arcShader.start();
		arcShader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		arcShader.viewMatrix.loadMatrix(view);
		arcShader.clipPlane.loadVec4(clipPlane);
		arcShader.sampler.loadTexUnit(0);
		arcShader.lightmap.loadTexUnit(1);
		arcShader.shadowMap.loadTexUnit(2);
		arcShader.bumpMap.loadTexUnit(3);
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];
			if (light == null) {
				arcShader.strength.loadFloat(i, 0f);
				continue;
			}
			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();
			
			Matrix4f lightSpaceMatrix = new Matrix4f();
			Matrix4f.mul(lightProjMatrix, light.getLightViewMatrix(), lightSpaceMatrix);
			
			arcShader.lightPos.loadVec3(i, pos.x, pos.y, pos.z);
			arcShader.lightDir.loadVec3(i, dir.x, dir.y, dir.z);
			arcShader.cutoff.loadVec2(i, light.getCutoff(), light.getOuterCutoff());
			arcShader.strength.loadFloat(i, light.getStrength());
			arcShader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);
			
			Resources.getTexture("shadow" + i).bind(2 + i);
		}
		
		if (Debug.fullbright || !hasLightmap) {
			Resources.getTexture("none").bind(1);
		} else {
			Resources.getTexture("lightmap").bind(1);
		}
	}
	
	public static void render(Camera camera, Architecture arc, Cluster cluster) {
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		Material material = arc.getTextures()[cluster.getDiffuseId()].getMaterial();
		
		switch(material) {
		case CAMERA:
			arc.callCommand("camview_render index=0");		// Tells the map to request a render at id 0
			renderCamView(cluster);
			break;
		default:
			renderDefault(cluster, arc);
		}
		
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public static void finishRender() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		arcShader.stop();
		lastTexture = null;
	}
	
	private static void renderDefault(Cluster cluster, Architecture arc) {
		Texture diffuse = arc.getTextures()[cluster.getDiffuseId()];
		if (lastTexture != diffuse) {
			lastTexture = diffuse;
			(Debug.ambientOnly ? Resources.NO_TEXTURE : diffuse).bind(0);
			
			int bumpMapId = cluster.getBumpMapId();
			arcShader.hasBumpMap.loadInt(bumpMapId);
			
			if (bumpMapId == 0) {
				Resources.NO_TEXTURE.bind(3);
			} else {
				arc.getTextures()[bumpMapId].bind(3);
			}
			
		}

		cluster.getModel().bind(0, 1, 2, 3);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cluster.getModel().getVertexCount());
		Render.drawCalls++;
	}

	private static void renderCamView(Cluster tMesh) {
		FrameBuffer refractionFbo = Render.getRefractionFbo();
		refractionFbo.bindTextureBuffer(0);

		tMesh.getModel().bind(0, 1, 2, 3);
		// arcShader.modelMatrix.loadMatrix(tMesh.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tMesh.getModel().getVertexCount());
		Render.drawCalls++;
	}
	
	public static void cleanUp() {
		arcShader.cleanUp();
	}
}
