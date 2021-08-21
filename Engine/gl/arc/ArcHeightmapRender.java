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
import gl.res.Model;
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
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];
			if (light == null) {
				heightmapShader.strength.loadFloat(i, 0f);
				continue;
			}
			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();
			
			Matrix4f lightSpaceMatrix = new Matrix4f();
			Matrix4f.mul(lightProjMatrix, light.getLightViewMatrix(), lightSpaceMatrix);
			
			heightmapShader.lightPos.loadVec3(i, pos.x, pos.y, pos.z);
			heightmapShader.lightDir.loadVec3(i, dir.x, dir.y, dir.z);
			heightmapShader.cutoff.loadVec2(i, light.getCutoff(), light.getOuterCutoff());
			heightmapShader.strength.loadFloat(i, light.getStrength());
			heightmapShader.lightSpaceMatrix.loadMatrix(i, lightSpaceMatrix);
			
			Resources.getTexture("shadow" + i).bind(3 + i);
		}
		
		if (Debug.fullbright || !hasLightmap) {
			Resources.getTexture("none").bind(2);
		} else {
			Resources.getTexture("lightmap").bind(2);
		}
		
		for(ArcHeightmap heightmap : heightmaps) {
			Model model = heightmap.getModel();
			model.bind(0,1,2);
			//int texId = arc.bsp.faces[heightmap.getFaceId()].texId;
			//int id = arc.getPackedAssets().getTextureData()[texId].textureId;

			if (Debug.ambientOnly) {
				Resources.getTexture("none").bind(0);
				Resources.getTexture("none").bind(1);
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
