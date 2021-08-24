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
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.vis.Cluster;
import scene.mapscene.MapScene;

public class ArcRenderLightGeneric {
	
	private static final DynamicLight[] NO_LIGHTS = new DynamicLight[] {null, null, null, null};
	
	private static ArcShaderBase baseShader;
	private static ArcShaderBump bumpShader;
	private static ArcShaderEnvMap envMapShader;
	private static ArcShaderBumpEnvMap bumpAndEnvMapShader;
	
	private static ArcShaderBase shader;

	private static int lastDiffuse = -1, lastBumpMap = -1, lastSpecMap = -1;

	public static boolean fullRender = false;
	
	public static void init() {
		baseShader = new ArcShaderBase();
		bumpShader = new ArcShaderBump();
		envMapShader = new ArcShaderEnvMap();
		bumpAndEnvMapShader = new ArcShaderBumpEnvMap();
		
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		
		shader = baseShader;
	}

	public static void restartShaders(Camera camera, Architecture arc, Vector4f clipPlane, Matrix4f lightProjMatrix, DynamicLight[] lights, boolean hasLightmap) {
		endShader(arc);
		shader = baseShader;
		
		startShader(camera, clipPlane, null, NO_LIGHTS, hasLightmap);
	}
	
	public static void startRender(MapScene scene, Architecture arc, Vector4f clipPlane, boolean hasLightmap,
			Matrix4f lightProjMatrix, DynamicLight[] lights, Cluster cluster) {
		
		Camera camera = scene.getCamera();
		
		int flags 	= ((cluster.getBumpMapId() != -1) ? 1 : 0)
					+ ((cluster.getSpecMapId() != -1) ? 2 : 0);
		
		ArcShaderBase nextShader = baseShader;
		switch(flags) {
		case 1:
			nextShader = bumpShader;
			break;
		case 2:
			nextShader = envMapShader;
			break;
		case 3:
			nextShader = bumpAndEnvMapShader;
			break;
		}
		if (nextShader == shader) {
			return;
		}

		endShader(arc);
		shader = nextShader;
		startShader(camera, clipPlane, lightProjMatrix, lights, hasLightmap);

		switch(flags) {
		case 1:
			bumpShader.bumpMap.loadTexUnit(3);
			break;
		case 2:
			envMapShader.specMap.loadTexUnit(2);
			envMapShader.envMap.loadTexUnit(4);
			
			envMapShader.cameraPos.loadVec3(camera.getPosition());
			break;
		case 3:
			bumpAndEnvMapShader.specMap.loadTexUnit(2);
			bumpAndEnvMapShader.bumpMap.loadTexUnit(3);
			bumpAndEnvMapShader.envMap.loadTexUnit(4);

			bumpAndEnvMapShader.cameraPos.loadVec3(camera.getPosition());
			break;
		}
	}

	public static void render(Camera camera, Architecture arc, Cluster cluster) {
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		int diffuseId = cluster.getDiffuseId();
		if (lastDiffuse != diffuseId) {
			Texture diffuse = arc.getTextures()[diffuseId];
			
			(Debug.ambientOnly ? Resources.NO_TEXTURE : diffuse).bind(0);
			
			int bumpMapId = cluster.getBumpMapId();
			int specMapId = cluster.getSpecMapId();
			
			if (bumpMapId == -1) {
				if (lastBumpMap != -1)
					arc.getTextures()[lastBumpMap].unbind(3);
			} else {
				arc.getTextures()[bumpMapId].bind(3);
			}
			
			if (specMapId == -1) {
				if (lastSpecMap != -1)
					arc.getTextures()[lastSpecMap].unbind(2);
			} else {
				arc.getTextures()[specMapId].bind(2);
			}
			
			lastDiffuse = diffuseId;
			lastBumpMap = bumpMapId;
			lastSpecMap = specMapId;
		}

		cluster.getModel().bind(0, 1, 2, 3);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cluster.getModel().getVertexCount());
		Render.drawCalls++;
		
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public static void startShader(Camera camera, Vector4f clipPlane, Matrix4f lightProjMatrix, DynamicLight[] lights, boolean hasLightmap) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.clipPlane.loadVec4(clipPlane);
		shader.sampler.loadTexUnit(0);
		shader.lightmap.loadTexUnit(1);
		shader.shadowMap.loadTexUnit(5);
		
		Texture.unbindTexture(0);
		Texture.unbindTexture(1);
		Texture.unbindTexture(2);
		Texture.unbindTexture(3);
		Texture.unbindTexture(4);
		Texture.unbindTexture(5);
		
		Resources.getTexture("skybox").bind(4);
		//scene.getArchitecture().getEnvironmentMap(camera.getPosition()).bind(5);
		Matrix4f lightPosMatrix = new Matrix4f();
		Matrix4f lightDirMatrix = new Matrix4f();
		float[] lightInfo = new float[] {0f, 0f, 0f, 0f};
		
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];
			
			if (light == null)
				continue;
			
			lightInfo[i] = light.getStrength();
			
			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();
			
			Matrix4f lightSpaceMatrix = new Matrix4f();
			Matrix4f.mul(lightProjMatrix, light.getLightViewMatrix(), lightSpaceMatrix);
			lightPosMatrix.setRow(new float[] {pos.x, pos.y, pos.z, light.getCutoff()}, i);
			lightDirMatrix.setRow(new float[] {dir.x, dir.y, dir.z, light.getOuterCutoff()}, i);
			shader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);
			
			Resources.getTexture("shadow" + i).bind(5 + i);
		}
		
		shader.lightInfo.loadVec4(lightInfo[0], lightInfo[1], lightInfo[2], lightInfo[3]);
		shader.lightPos.loadMatrix(lightPosMatrix);
		shader.lightDir.loadMatrix(lightDirMatrix);
		
		if (Debug.fullbright || !hasLightmap) {
			Resources.getTexture("none").bind(1);
		} else {
			Resources.getTexture("lightmap").bind(1);
		}
	}
	
	public static void endShader(Architecture arc) {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL30.glBindVertexArray(0);
		shader.stop();
		
		if (lastBumpMap != -1)
			arc.getTextures()[lastBumpMap].unbind(3);
		if (lastSpecMap != -1)
			arc.getTextures()[lastSpecMap].unbind(4);
		
		lastDiffuse = -1;
		lastBumpMap = -1;
		lastSpecMap = -1;
	}
	
	public static void cleanUp() {
		baseShader.cleanUp();
		bumpShader.cleanUp();
		envMapShader.cleanUp();
		bumpAndEnvMapShader.cleanUp();
	}
}
