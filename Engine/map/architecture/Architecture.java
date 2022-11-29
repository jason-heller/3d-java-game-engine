package map.architecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import core.Resources;
import dev.Debug;
import dev.cmd.Console;
import geom.AxisAlignedBBox;
import geom.CollideUtils;
import geom.MTV;
import geom.Plane;
import gl.Camera;
import gl.Render;
import gl.arc.ArcFaceRender;
import gl.arc.ArcRenderMaster;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.particle.ParticleEmitter;
import gl.res.Texture;
import gl.skybox._3d.SkyboxCamera;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcHeightmap;
import map.architecture.components.ArcLightCube;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcTextureData;
import map.architecture.components.ArcTriggerClip;
import map.architecture.functions.ArcFuncHandler;
import map.architecture.functions.ArcFunction;
import map.architecture.functions.commands.CamView;
import map.architecture.util.BspRaycast;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.Pvs;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.util.PhysicsEntity;
import scene.mapscene.MapScene;

public class Architecture {

	private Scene scene;
	private String mapName;
	private byte mapVersion;
	private byte mapCompilerVersion;
	
	public Bsp bsp;
	public Pvs pvs;
	
	private DynamicLightHandler dynamicLightHandler;
	
	private ArcNavigation navigation;
	private Map<Entity, ArcTriggerClip> activeTriggers;
	
	private ActiveLeaves activeLeaves = new ActiveLeaves();
	private BspLeaf currentLeaf = null;
	
	public Vector3f[] vertices;
	private List<ParticleEmitter> emitters = new ArrayList<ParticleEmitter>();
	
	private ArcTextureData textureData;
	
	private ArcFuncHandler funcHandler;
	private Lightmap lightmap;
	public ArcLightCube[] ambientLightCubes;
	
	public boolean hasSkybox = false;
	private SkyboxCamera skyCamera = null;
	
	private LinkedList<BspLeaf> audible = new LinkedList<>();
	
	private List<ArcHeightmap> renderedHeightmaps = new ArrayList<>();
	private Map<Integer, Texture> environmentMaps;
	
	
	public Architecture(Scene scene) {
		this.scene = scene;	
		funcHandler = new ArcFuncHandler();
		lightmap = new Lightmap();
		activeTriggers = new HashMap<>();
		dynamicLightHandler = new DynamicLightHandler();
	}
	
	public void determineVisibleLeafs(Camera camera) {

		BspLeaf cameraLeaf = bsp.walk(camera.getPosition());
		if (cameraLeaf.clusterId != -1 && cameraLeaf != currentLeaf) {
			currentLeaf = cameraLeaf;
			activeLeaves.clear();
			renderedHeightmaps.clear();
			
			int[] vis = pvs.getData(cameraLeaf, 0);
			int[] pas = pvs.getData(cameraLeaf, 1);
			audible.clear();
			
			for(int i = 0; i < bsp.leaves.length; i++) {
				BspLeaf leaf = bsp.leaves[i];
				if (leaf.clusterId == -1) continue;
				
				if (pas[leaf.clusterId] != 0) {
					audible.add(leaf);
				}
				
				if (vis[leaf.clusterId] == 0 && !ArcFaceRender.fullRender) {
					continue;
				}
				
				activeLeaves.addLeaf(camera, leaf);
				for(short heightmap : leaf.heightmaps) {
					renderedHeightmaps.add(bsp.heightmaps[heightmap]);
				}
			}	
			
			if (CamView.requestRender) {
				BspLeaf camViewLeaf = bsp.walk(CamView.renderPos);
				if (camViewLeaf.clusterId == -1) return;
				vis = pvs.getData(camViewLeaf, 0);
				
				for(int i = 0; i < bsp.leaves.length; i++) {
					BspLeaf leaf = bsp.leaves[i];
					if (leaf.clusterId == -1) continue;
					if (vis[leaf.clusterId] == 0) continue;
					activeLeaves.addLeaf(camera, leaf);
				}	
			}
			
			//renderedLeaves.clear();
			//renderedLeaves.addAll(renderedNew);
		}
	}
	
	public List<BspLeaf> getCluster(BspLeaf originLeaf) {
		List<BspLeaf> leaves = new ArrayList<>();
		if (originLeaf.clusterId != -1) {
			int[] vis = pvs.getData(originLeaf, 0);
			
			for(int i = 0; i < bsp.leaves.length; i++) {
				BspLeaf leaf = bsp.leaves[i];
				if (leaf.clusterId == -1) continue;
				
				if (vis[leaf.clusterId] == 0) {
					continue;
				}
				
				leaves.add(leaf);
			}
		}
		return leaves;
	}
	
	public void render(Camera camera, Vector4f clipPlane, boolean hasLighting, boolean withShaders) {
		
		if (withShaders)
			ArcRenderMaster.render((MapScene) scene, clipPlane, this, hasLighting, dynamicLightHandler.getLights(), activeLeaves);
		else 
			ArcRenderMaster.fastRender((MapScene) scene, clipPlane, this, hasLighting, dynamicLightHandler.getLights(), activeLeaves);
		
		for (ParticleEmitter pe : emitters) {
			pe.generateParticles(camera);
		}
		
		if (Debug.showAmbient) {
			int len = currentLeaf.firstAmbientSample + currentLeaf.numAmbientSamples;
			for(int i = currentLeaf.firstAmbientSample; i < len; i++) {
				ArcLightCube lightCube = ambientLightCubes[i];
				Vector3f pos = lightCube.getPosition(currentLeaf);
				Matrix4f matrix = new Matrix4f();
				matrix.translate(pos);
				matrix.scale(2f);
				Render.renderModel(Resources.getMesh("cube"), Resources.NO_TEXTURE, matrix, lightCube.getLighting());
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
				
				Plane[] planes = new Plane[trigger.numPlanes];
				for(int i = 0; i < trigger.numPlanes; i++) 
					planes[i] = bsp.planes[bsp.clipPlaneIndices[trigger.firstPlane + i]];
				
				MTV mtv = CollideUtils.convexHullBoxCollide(planes, physEnt.getBBox());
				if (mtv == null) {
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
	
	public Vector3f[] getLightsAt(Vector3f position) {
		if (lightmap.isActive() && !Debug.fullbright) {
			BspLeaf leaf = bsp.walk(position);
			return getLightsAt(position, leaf);
		}
		
		return ArcLightCube.FULLBRIGHT;
		
	}
	
	private Vector3f[] getLightsAt(Vector3f position, BspLeaf leaf) {
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
			return ArcLightCube.NO_LIGHT;
		}
		
		return nearestCube.getLighting();
	}
	
	public boolean isLeafAudible(BspLeaf leaf) {
		return audible.contains(leaf);
	}
	
	public void cleanUp() {
		bsp.cleanUp();
		final Texture[] textures = textureData.getTextures();
		for(Texture texture : textures) 
			texture.cleanUp();
		
		if (environmentMaps != null) {
			for(Texture texture : environmentMaps.values()) 
				texture.cleanUp();
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

	public ActiveLeaves getActiveLeaves() {
		return activeLeaves;
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

	public Texture[] getTextures() {
		return textureData.getTextures();
	}
	
	public String[] getTextureNames() {
		return textureData.getTextureNames();
	}

	public Lightmap getLightmap() {
		return lightmap;
	}
	
	public void changeMipmapBias() {
		for (Texture texture : this.getTextures()) {
			if (texture == null) continue;
			texture.bind(0);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, Render.defaultBias);
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void removeLight(DynamicLight light) {
		this.dynamicLightHandler.removeLight(light);
	}
	
	public DynamicLight addLight(Vector3f pos, Vector3f rot, float strength) {
		return this.dynamicLightHandler.addLight(pos, rot, strength);
	}

	/** Raycasts against the visible geometry
	 * @param pos the ray's origin
	 * @param dir the ray's direction
	 * @return the distance the ray traveled and face, or null if nothing was hit
	 */
	public BspRaycast raycast(Vector3f org, Vector3f dir) {
		float shortestDist = Float.POSITIVE_INFINITY;
		ArcFace collidedFace = null;

		activeLeaves.beginIteration();
		while(activeLeaves.hasNext()) {
			BspLeaf leaf = activeLeaves.next();
			
			Vector3f bounds = Vector3f.sub(leaf.max, leaf.min).div(2f);
			Vector3f center = Vector3f.add(leaf.min, bounds);
			
			float distLeaf = new AxisAlignedBBox(center, bounds).collide(org, dir);
			
			if (Float.isInfinite(distLeaf))
				continue;
			
			if (distLeaf < shortestDist) {
				
				ArcFace[] faces = bsp.getFaces(leaf);
				for(ArcFace face : faces) {
					
					if (face.texMapping == -1) continue;
					//ArcTextureMapping texData = bsp.getTextureMappings()[face.texMapping];
					//if (texData.textureId == -1) continue;
					
					float dist = CollideUtils.convexPolygonRay(bsp, face, org, dir);
					if (dist < shortestDist) {
						shortestDist = dist;
						collidedFace = face;
					}
				}
			}
		}
		return collidedFace == null ? null : new BspRaycast(collidedFace, shortestDist);
	}

	public DynamicLightHandler getDynamicLightHandler() {
		return this.dynamicLightHandler;
	}

	public void setSkyCamera(SkyboxCamera skyCamera) {
		this.skyCamera = skyCamera;
	}
	
	public SkyboxCamera getSkyCamera() {
		return skyCamera;
	}

	public List<ArcHeightmap> getRenderedHeightmaps() {
		return renderedHeightmaps;
	}

	public void setTextureData(ArcTextureData textureData) {
		this.textureData = textureData;
	}

	public List<BspLeaf> getVisibleLeavesIntersecting(AxisAlignedBBox box) {
		Vector3f max = Vector3f.add(box.getCenter(), box.getBounds());
		Vector3f min = Vector3f.sub(box.getCenter(), box.getBounds());
		activeLeaves.beginIteration();
		List<BspLeaf> leaves = new ArrayList<>();
		while(activeLeaves.hasNext()) {
			BspLeaf leaf = activeLeaves.next();
			if (leaf.intersects(max, min)) {
				leaves.add(leaf);
			}
		}
		return leaves;
	}

	public void setEnvironmentMaps(Map<Integer, Texture> environmentMaps) {
		this.environmentMaps = environmentMaps;
		Console.log("Loaded environment maps");
	}
	
	public Texture getEnvironmentMap(Vector3f position, AtomicInteger clipId) {
		float closest = Float.POSITIVE_INFINITY;
		int clipIndex = -1;
		
		if (environmentMaps == null)
			return Resources.getTexture("skybox");
		
		for(int index : environmentMaps.keySet()) {
			ArcClip clip = bsp.clips[index];
			Vector3f center = clip.bbox.getCenter();
			
			float dist = Vector3f.distanceSquared(center, position);
			
			if (dist < closest) {
				closest = dist;
				clipIndex = index;
			}
		}
		
		if (clipIndex == -1)
			return Resources.getTexture("skybox");
		
		clipId.set(clipIndex);
		return environmentMaps.get(clipIndex);
	}
}
