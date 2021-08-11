package gl.arc;

import java.util.List;

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
import gl.TexturedModel;
import gl.fbo.FrameBuffer;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.res.Model;
import gl.res.Texture;
import gl.shadow.ShadowRender;
import gl.water.WaterShader;
import map.architecture.Architecture;
import map.architecture.Material;
import map.architecture.components.ArcHeightmap;
import map.architecture.vis.BspLeaf;

public class ArcRender {
	
	private static ArcShader arcShader;
	private static ArcHeightmapShader heightmapShader;
	private static WaterShader waterShader;
	private static ShadowRender shadowRender;
	
	private static Texture lastTexture = null;
	
	public static boolean fullRender = false;
	
	public static void init() {
		arcShader = new ArcShader();
		heightmapShader = new ArcHeightmapShader();
		waterShader = new WaterShader();
		shadowRender = new ShadowRender();
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
	}
	
	public static void startRender(Camera camera, Vector4f clipPlane, boolean hasLightmap, DynamicLight[] lights) {
		Matrix4f view = camera.getViewMatrix();

		arcShader.start();
		arcShader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		arcShader.viewMatrix.loadMatrix(view);
		arcShader.clipPlane.loadVec4(clipPlane);
		arcShader.sampler.loadTexUnit(0);
		arcShader.lightmap.loadTexUnit(1);
		arcShader.shadowMap.loadTexUnit(2);
		
		for(int i = 0; i < DynamicLightHandler.MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight light = lights[i];
			if (light == null) {
				arcShader.strength.loadFloat(i, 0f);
				continue;
			}
			final Vector3f pos = light.getPosition();
			final Vector3f dir = light.getViewDirection();
			
			Matrix4f lightSpaceMatrix = new Matrix4f();
			Matrix4f.mul(shadowRender.getLightProjectionMatrix(), light.getLightViewMatrix(), lightSpaceMatrix);
			
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
	
	public static void render(Camera camera, Architecture arc, TexturedModel tMesh) {
		if (Debug.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		Material material = tMesh.getTexture().getMaterial();
		
		switch(material) {
		case CAMERA:
			arc.callCommand("camview_render index=0");		// Tells the map to request a render at id 0
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
	
	public static void finishRender() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		arcShader.stop();
		lastTexture = null;
	}
	
	private static void renderDefault(TexturedModel tMesh) {
		bindUnique(tMesh.getTexture());

		tMesh.getModel().bind(0,1,2);
		arcShader.modelMatrix.loadMatrix(tMesh.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tMesh.getModel().getVertexCount());
		Render.drawCalls++;
	}
	
	private static void bindUnique(Texture texture) {
		if (lastTexture != texture) {
			lastTexture = texture;
			(Debug.ambientOnly ? Resources.getTexture("none") : texture).bind(0);
		}
	}

	private static void renderCamView(TexturedModel tMesh) {
		FrameBuffer refractionFbo = Render.getRefractionFbo();
		refractionFbo.bindTextureBuffer(0);
		
		tMesh.getModel().bind(0,1,2);
		arcShader.modelMatrix.loadMatrix(tMesh.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, tMesh.getModel().getVertexCount());
		Render.drawCalls++;
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
	
	public static void renderShadows(Camera camera, List<BspLeaf> renderedLeaves, DynamicLight[] lights) {
		shadowRender.render(lights, renderedLeaves);
	}
	
	public static void renderHeightmaps(Camera camera, Architecture arc, List<ArcHeightmap> heightmaps, Vector4f clipPlane, boolean hasLightmap, DynamicLight[] lights) {
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
			Matrix4f.mul(shadowRender.getLightProjectionMatrix(), light.getLightViewMatrix(), lightSpaceMatrix);
			
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
				String tex = arc.getMapTextureRefs()[heightmap.getTexture1()];
				Resources.getTexture(tex).bind(0);
				tex = arc.getMapTextureRefs()[heightmap.getTexture2()];
				Resources.getTexture(tex).bind(1);
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
		arcShader.cleanUp();
		heightmapShader.cleanUp();
		waterShader.cleanUp();
		shadowRender.cleanUp();
	}

	public static Matrix4f getShadowProjectionMatrix() {
		return shadowRender.getLightProjectionMatrix();
	}
}
