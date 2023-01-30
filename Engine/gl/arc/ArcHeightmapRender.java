package gl.arc;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.res.Mesh;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.components.ArcHeightmap;

public class ArcHeightmapRender {

	private static ArcHeightmapShader heightmapShader;
	
	public static void init() {
		heightmapShader = new ArcHeightmapShader();
	}
	
	public static void renderHeightmaps(Camera camera, Architecture arc, List<ArcHeightmap> heightmaps, Vector4f clipPlane, boolean hasLightmap, Matrix4f lightProjMatrix, DynamicLight[] lights) {
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		heightmapShader.start();
		heightmapShader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		heightmapShader.viewMatrix.loadMatrix(camera.getViewMatrix());
		heightmapShader.clipPlane.loadVec4(clipPlane);
		heightmapShader.sampler1.loadTexUnit(0);
		heightmapShader.sampler2.loadTexUnit(1);
		heightmapShader.lightmap.loadTexUnit(2);
		heightmapShader.shadowMap.loadTexUnit(3);
		
		Matrix4f lightPosMatrix = new Matrix4f();
		Matrix4f lightDirMatrix = new Matrix4f();
		float[] lightInfo = new float[] { 0f, 0f, 0f, 0f };
		
		for (int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];

			if (light == null)
				continue;

			lightInfo[i] = light.getStrength();

			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();

			Matrix4f lightSpaceMatrix = new Matrix4f();
			lightProjMatrix.mul(light.getLightViewMatrix(), lightSpaceMatrix);
			lightPosMatrix.setRow(i, new Vector4f(pos.x, pos.y, pos.z, light.getCutoff()));
			lightDirMatrix.setRow(i, new Vector4f(dir.x, dir.y, dir.z, light.getOuterCutoff()));

			heightmapShader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);

			Resources.getTexture("shadow" + i).bind(3 + i);
		}
		
		heightmapShader.lightInfo.loadVec4(lightInfo[0], lightInfo[1], lightInfo[2], lightInfo[3]);
		heightmapShader.lightPos.loadMatrix(lightPosMatrix);
		heightmapShader.lightDir.loadMatrix(lightDirMatrix);
		
		if (Debug.fullbright || !hasLightmap) {
			Resources.NO_TEXTURE.bind(2);
		} else {
			Resources.getTexture("lightmap").bind(2);
		}
		
		for(ArcHeightmap heightmap : heightmaps) {
			Mesh model = heightmap.getModel();
			model.bind(0,1,2);

			if (Debug.ambientOnly) {
				Resources.NO_TEXTURE.bind(0);
				Resources.NO_TEXTURE.bind(1);
			} else {
				Texture tex = arc.getTextures()[heightmap.getTexture1()];
				tex.bind(0);
				tex = arc.getTextures()[heightmap.getTexture2()];
				tex.bind(1);
			}
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		
		heightmapShader.stop();
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}
	

	public static void cleanUp() {
		heightmapShader.cleanUp();
	}
}
