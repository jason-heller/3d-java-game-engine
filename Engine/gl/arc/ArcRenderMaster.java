package gl.arc;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.light.DynamicLight;
import gl.line.LineRender;
import gl.shadow.ShadowRender;
import gl.water.WaterRender;
import map.architecture.Architecture;
import map.architecture.components.ArcClip;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Cluster;
import scene.Scene;
import scene.mapscene.MapScene;

public class ArcRenderMaster {

	private static ShadowRender shadowRender;
	
	public static void init() {
		ArcRenderLightGeneric.init();
		WaterRender.init();
		ArcHeightmapRender.init();

		shadowRender = new ShadowRender();
		
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
	}
	
	public static void render(Scene scene, Vector4f clipPlane, Architecture arc, boolean hasLightmap, DynamicLight[] lights, List<BspLeaf> renderedLeaves) {
		Camera camera = scene.getCamera();
		Bsp bsp = arc.bsp;
		
		shadowRender.render(lights, renderedLeaves);
		Matrix4f lightProjMatrix = shadowRender.getLightProjectionMatrix();
		
		bsp.objects.render(camera, arc, clipPlane, lightProjMatrix);
		
		ArcHeightmapRender.renderHeightmaps(camera, arc, arc.getRenderedHeightmaps(), clipPlane, hasLightmap,
				lightProjMatrix, lights);

		ArcRenderLightGeneric.restartShaders(camera, arc, clipPlane, lightProjMatrix, lights, hasLightmap);

		for(BspLeaf leaf : renderedLeaves) {
			
			if (leaf.isUnderwater && clipPlane.w == Float.POSITIVE_INFINITY) {
				Render.renderWaterFbos(scene, camera, leaf.max.y);
				WaterRender.renderWater(camera, leaf.max, leaf.min);
			}
			
			if (Debug.showClips) {
				for(short id : leaf.clips ) {
					ArcClip clip = bsp.clips[id];
					LineRender.drawBox(clip.bbox.getCenter(), clip.bbox.getBounds(), clip.id.getColor());
				}
			}
			
			for(Cluster cluster : leaf.getMeshes()) {
				if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min))
					continue;

				//Vector3f center = Vector3f.add(leaf.max, leaf.min).mul(.5f);
				//if (Vector3f.distanceSquared(camera.getPosition(), center) > 300*300)
				//	ArcRenderLightGeneric.startRender((MapScene) scene, arc, clipPlane, hasLightmap);
				//else
				ArcRenderLightGeneric.startRender((MapScene) scene, arc, clipPlane, hasLightmap, lightProjMatrix,
						lights, cluster);

				ArcRenderLightGeneric.render(camera, arc, cluster);
				// ArcRenderLightGeneric.finishRender(arc);
			}
		}
		
		
		
	}
	
	public static void cleanUp() {
		ArcRenderLightGeneric.cleanUp();
		WaterRender.cleanUp();
		ArcHeightmapRender.cleanUp();
		shadowRender.cleanUp();
	}

	public static Matrix4f getShadowProjectionMatrix() {
		return shadowRender.getLightProjectionMatrix();
	}
}
