package map.architecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.line.LineRender;
import gl.map.architecture.render.ArcRender;
import gl.particle.ParticleEmitter;
import gl.res.Texture;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcLightCube;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcPackedAssets;
import map.architecture.components.ArcTextureData;
import map.architecture.components.ArcTriggerClip;
import map.architecture.functions.ArcFuncHandler;
import map.architecture.functions.ArcFunction;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Pvs;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.util.PhysicsEntity;

public class Architecture {

	private Scene scene;
	private String mapName;
	private byte mapVersion;
	private byte mapCompilerVersion;
	
	public Bsp bsp;
	public Pvs pvs;
	
	private ArcNavigation navigation;
	private Map<Entity, ArcTriggerClip> activeTriggers;
	
	private List<BspLeaf> renderedLeaves = new ArrayList<BspLeaf>();
	private BspLeaf currentLeaf = null;
	
	public Vector3f[] vertices;
	public ArcFace[] faces;
	
	private List<ParticleEmitter> emitters = new ArrayList<ParticleEmitter>();
	private Texture[] mapSpecificTextures;

	private Vector3f sunVector;
	private String[] mapSpecTexRefs;
	
	private ArcPackedAssets packedAssets;
	private ArcFuncHandler funcHandler;
	private Lightmap lightmap;
	public ArcLightCube[] ambientLightCubes;
	public boolean hasSkybox = false;
	
	public Architecture(Scene scene) {
		this.scene = scene;	
		funcHandler = new ArcFuncHandler();
		lightmap = new Lightmap();
		activeTriggers = new HashMap<>();
	}
	
	public void determineVisibleLeafs(Camera camera) {
		BspLeaf cameraLeaf = bsp.walk(camera.getPosition());
		if (cameraLeaf.clusterId != -1 && cameraLeaf != currentLeaf) {
			currentLeaf = cameraLeaf;
			renderedLeaves.clear();
			
			int[] vis = pvs.getClustersToRender(cameraLeaf);
			
			for(int i = 0; i < bsp.leaves.length; i++) {
				BspLeaf leaf = bsp.leaves[i];
				if (leaf.clusterId == -1) continue;
				if (vis[leaf.clusterId] == 0) continue;
				renderedLeaves.add(leaf);
			}
				
		}
	}
	
	public void render(Camera camera, float clipX, float clipY, float clipZ, float clipDist) {
		
		bsp.objects.render(camera, this);
		
		for(BspLeaf leaf : renderedLeaves) {
			
			if (leaf.isUnderwater && clipDist == Float.POSITIVE_INFINITY) {
				Render.renderWaterFbos(scene, camera, leaf.max.y);
				ArcRender.renderWater(camera, leaf.max, leaf.min);
			}
			
			if (Debug.showClips) {
				for(short id : leaf.clips ) {
					ArcClip clip = bsp.clips[id];
					LineRender.drawBox(clip.bbox.getCenter(), clip.bbox.getBounds(), clip.id.getColor());
				}
			}
			
			ArcRender.startRender(camera.getProjectionMatrix(), camera.getViewMatrix(), clipX, clipY, clipZ, clipDist);
			
			for(TexturedModel visObj : leaf.getMeshes()) {
				
				if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min)) {continue;}

				ArcRender.render(visObj);
			}
			
			ArcRender.finishRender();
		}
		
		for (ParticleEmitter pe : emitters) {
			pe.generateParticles(camera);
		}
		
		if (Debug.showAmbient) {
			int len = currentLeaf.firstAmbientSample + currentLeaf.numAmbientSamples;
			for(int i = currentLeaf.firstAmbientSample; i < len; i++) {
				ArcLightCube lightCube = ambientLightCubes[i];
				Vector3f pos = lightCube.getPosition(currentLeaf);
				LineRender.drawLine(pos, Vector3f.add(pos, new Vector3f(-1,0,0)), lightCube.getColor(0));
				LineRender.drawLine(pos, Vector3f.add(pos, new Vector3f(1,0,0)), lightCube.getColor(1));
				LineRender.drawLine(pos, Vector3f.add(pos, new Vector3f(0,-1,0)), lightCube.getColor(2));
				LineRender.drawLine(pos, Vector3f.add(pos, new Vector3f(0,1,0)), lightCube.getColor(3));
				LineRender.drawLine(pos, Vector3f.add(pos, new Vector3f(0,0,-1)), lightCube.getColor(4));
				LineRender.drawLine(pos, Vector3f.add(pos, new Vector3f(0,0,1)), lightCube.getColor(5));
			}
		}
		
	}
	
	public void pollTriggers() {
		Iterator<Entity> iter = activeTriggers.keySet().iterator();
		while(iter.hasNext()) {
			Entity entity = iter.next();
			ArcTriggerClip trigger = activeTriggers.get(entity);
			
			if (entity instanceof PhysicsEntity) {
				PhysicsEntity physEnt = (PhysicsEntity)entity;
				if (trigger.bbox.collide(physEnt.getBBox()) == null) {
					trigger.interact(entity, false);
					iter.remove();
				}
			} else {
				if (!trigger.bbox.collide(entity.pos)) {
					trigger.interact(entity, false);
					iter.remove();
				}
			}
		}
	}
	
	public Vector3f getLightAtPosition(Vector3f position, Vector3f direction) {
		BspLeaf leaf = bsp.walk(position);
		return getLightAtPosition(position, direction, leaf);
	}
	
	private Vector3f getLightAtPosition(Vector3f position, Vector3f direction, BspLeaf leaf) {
		ArcLightCube nearestCube = null;//ambientLightCubes[leaf.firstAmbientSample];
		float nearest = Float.POSITIVE_INFINITY;//Vector3f.distanceSquared(ambientLightCubes[leaf.firstAmbientSample].getPosition(leaf), position);
		
		for(int i = 0; i < leaf.numAmbientSamples; i++) {
			int sampleId = leaf.firstAmbientSample + i;
			ArcLightCube lightCube = ambientLightCubes[sampleId];
			float dist = Vector3f.distanceSquared(lightCube.getPosition(leaf), position);
			
			if (dist < nearest) {
				nearest = dist;
				nearestCube = lightCube;
			}
		}
		
		if (nearestCube == null) {
			return new Vector3f();
		}
		
		return nearestCube.applyLighting(direction);
	}

	public void passAssetsToOpenGL() {
		packedAssets.passToOpenGL();
		packedAssets = null;
		callCommand("spawn_player");
	}
	
	public void cleanUp() {
		bsp.cleanUp();
		for(int i = 0; i < mapSpecTexRefs.length; i++) {
			Resources.removeTexture(mapSpecTexRefs[i++]);
		}
		lightmap.delete();
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void setProperties(String mapName, byte mapVersion, byte mapCompilerVersion) {
		this.mapName = mapName;
		this.mapVersion = mapVersion;
		this.mapCompilerVersion = mapCompilerVersion;
	}

	public String getMapName() {
		return mapName;
	}

	public byte getMapVersion() {
		return mapVersion;
	}

	public byte getMapCompilerVersion() {
		return mapCompilerVersion;
	}

	public void setMapSpecificTextures(Texture[] mapSpecificTextures, String[] mapSpecTexRefs) {
		this.mapSpecificTextures = mapSpecificTextures;
		this.mapSpecTexRefs = mapSpecTexRefs;
	}

	public List<BspLeaf> getRenderedLeaves() {
		return renderedLeaves;
	}
	
	public Vector3f getSunVector() {
		return sunVector;
	}

	public void setSunVector(Vector3f sunVector) {
		this.sunVector = sunVector;
	}

	public void setPackedAssets(ArcPackedAssets packedAssets) {
		this.packedAssets = packedAssets;
	}
	
	public void addFunction(ArcFunction func) {
		funcHandler.add(func);
	}
	
	public void callCommand(Entity caller, String cmd) {
		funcHandler.callCommand(caller.pos, cmd);
	}
	
	public void callCommand(String cmd) {
		funcHandler.callCommand(Vector3f.ZERO, cmd);
	}

	public void createLightmap(byte[] rgb, ArcFace[] faces) {
		lightmap.create(rgb, faces);
	}

	public void setNavigation(ArcNavigation navigation) {
		this.navigation = navigation;
	}
	
	public ArcNavigation getNavigation() {
		return navigation;
	}

	public ArcTriggerClip getActiveTrigger(Entity entity) {
		return activeTriggers.get(entity);
	}

	public void setTriggerActive(Entity entity, ArcTriggerClip clip) {
		activeTriggers.put(entity, clip);
	}

	public String[] getMapTextureRefs() {
		return this.mapSpecTexRefs;
	}

	public ArcPackedAssets getPackedAssets() {
		return packedAssets;
	}

	public ArcTextureData[] getTexData() {
		return this.packedAssets.getTextureData();
	}
	
	public Texture[] getReferencedTextures() {
		return this.mapSpecificTextures;
	}
	
	public String[] getReferencedTexNames() {
		return mapSpecTexRefs;
	}

	public Lightmap getLightmap() {
		return lightmap;
	}
	
	public void changeMipmapBias() {
		for (Texture texture : this.getReferencedTextures()) {
			if (texture == null) continue;
			texture.bind(0);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, Render.defaultBias);
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	} 
}
