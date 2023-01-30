package gl.arc;

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
import map.architecture.ActiveLeaves;
import map.architecture.Architecture;
import map.architecture.components.ArcClip;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Cluster;
import scene.mapscene.MapScene;

public class ArcRenderMaster {

	private static ShadowRender shadowRender;
	
	public static void init() {
		ArcFaceRender.init();
		WaterRender.init();
		ArcHeightmapRender.init();

		shadowRender = new ShadowRender();
		
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
	}
	
	public static void render(MapScene scene, Vector4f clipPlane, Architecture arc, boolean hasLightmap, DynamicLight[] lights, ActiveLeaves activeLeaves) {
		Camera camera = scene.getCamera();
		Bsp bsp = arc.bsp;
		
		shadowRender.render(lights, activeLeaves.getNear());
		Matrix4f lightProjMatrix = shadowRender.getLightProjectionMatrix();
		
		bsp.objects.render(camera, arc, clipPlane, lightProjMatrix);
		
		ArcHeightmapRender.renderHeightmaps(camera, arc, arc.getRenderedHeightmaps(), clipPlane, hasLightmap,
				lightProjMatrix, lights);

		ArcFaceRender.restartShaders(scene, arc, clipPlane, lightProjMatrix, lights, hasLightmap);

		for(BspLeaf leaf : activeLeaves.getNear()) {
			if (leaf.isUnderwater && clipPlane.w == Float.POSITIVE_INFINITY) {
				Render.renderWaterFbos(scene, camera, leaf.max.y);
				WaterRender.renderWater(camera, leaf.max, leaf.min);
				ArcFaceRender.restartShaders(scene, arc, clipPlane, lightProjMatrix, lights, hasLightmap);
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

				ArcFaceRender.startRender(scene, arc, clipPlane, hasLightmap, lightProjMatrix, lights, cluster);

				ArcFaceRender.render(camera, arc, cluster);
			}
		}
		
		ArcFaceRender.startFastRender(scene, arc, clipPlane);
		for(BspLeaf leaf : activeLeaves.getFar()) {
			if (leaf.isUnderwater && clipPlane.w == Float.POSITIVE_INFINITY) {
				Render.renderWaterFbos(scene, camera, leaf.max.y);
				WaterRender.renderWater(camera, leaf.max, leaf.min);
				ArcFaceRender.restartShaders(scene, arc, clipPlane, lightProjMatrix, lights, hasLightmap);
			}
			
			for(Cluster cluster : leaf.getMeshes()) {
				if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min))
					continue;
				ArcFaceRender.fastRender(camera, arc, cluster);
			}
		}
		ArcFaceRender.endFastRender();
	}
	
	public static void fastRender(MapScene scene, Vector4f clipPlane, Architecture arc, boolean hasLightmap, DynamicLight[] lights, ActiveLeaves activeLeaves) {
		Camera camera = scene.getCamera();
		Bsp bsp = arc.bsp;

		Matrix4f lightProjMatrix = shadowRender.getLightProjectionMatrix();
		
		bsp.objects.render(camera, arc, clipPlane, lightProjMatrix);
		
		// ArcHeightmapRender.renderHeightmaps(camera, arc, arc.getRenderedHeightmaps(), clipPlane, hasLightmap,lightProjMatrix, lights);

		ArcFaceRender.startFastRender(scene, arc, clipPlane);
		
		for(BspLeaf leaf : activeLeaves.getFar()) {
			if (leaf.isUnderwater && clipPlane.w == Float.POSITIVE_INFINITY) {
				continue;
			}
			
			for(Cluster cluster : leaf.getMeshes()) {
				if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min))
					continue;
				ArcFaceRender.fastRender(camera, arc, cluster);
			}
		}
		
		for(BspLeaf leaf : activeLeaves.getNear()) {
			if (leaf.isUnderwater && clipPlane.w == Float.POSITIVE_INFINITY) {
				continue;
			}
			
			for(Cluster cluster : leaf.getMeshes()) {
				if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min))
					continue;
				ArcFaceRender.fastRender(camera, arc, cluster);
			}
		}
		
		ArcFaceRender.endFastRender();
	}
	
	public static void cleanUp(Architecture arc) {
		ArcFaceRender.cleanUp(arc);
		WaterRender.cleanUp();
		ArcHeightmapRender.cleanUp();
		shadowRender.cleanUp();
	}

	public static Matrix4f getShadowProjectionMatrix() {
		return shadowRender.getLightProjectionMatrix();
	}
}
