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

import core.App;
import core.Resources;
import dev.Debug;
import dev.RailBuilder;
import dev.cmd.Console;
import geom.AABB;
import geom.CollideUtils;
import geom.MTV;
import geom.Plane;
import gl.Camera;
import gl.Render;
import gl.arc.ArcFaceRender;
import gl.arc.ArcRenderMaster;
import gl.light.DynamicLight;
import gl.light.DynamicLightHandler;
import gl.line.LineRender;
import gl.particle.ParticleEmitter;
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import gl.skybox._3d.SkyboxCamera;
import map.Rail;
import map.RailList;
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
import scene.PlayableScene;
import scene.Scene;
import scene.entity.Entity;
import scene.mapscene.MapScene;
import util.Colors;
import util.GeomUtil;
import util.Vectors;

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
	private RailList[] railList;
	
	public List<Model> models;

	public Architecture(Scene scene) {
		this.scene = scene;	
		
		funcHandler = new ArcFuncHandler();
		lightmap = new Lightmap();
		activeTriggers = new HashMap<>();
		models = new ArrayList<>();
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
		
		if (Debug.railMode) {
			RailBuilder.update(camera, this);
		}
		
		if (Debug.showAmbient) {
			BspLeaf leaf = bsp.walk(((PlayableScene)App.scene).getPlayer().position);
			int len = leaf.firstAmbientSample + leaf.numAmbientSamples;
			for(int i = leaf.firstAmbientSample; i < len; i++) {
				ArcLightCube lightCube = ambientLightCubes[i];
				Vector3f pos = lightCube.getPosition(leaf);
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
			
			Plane[] planes = new Plane[trigger.planes.length];
			for(int i = 0; i < planes.length; i++) 
				planes[i] = bsp.planes[trigger.planes[i]];
			
			MTV mtv = CollideUtils.convexHullBoxCollide(planes, entity.getBBox());
			if (mtv == null) {
				trigger.interact(entity, false);
				iter.remove();
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
		float nearest = Float.POSITIVE_INFINITY;//Vectors.distanceSquared(ambientLightCubes[leaf.firstAmbientSample].getPosition(leaf), position);
		
		for(int i = 0; i < leaf.numAmbientSamples; i++) {
			int sampleId = leaf.firstAmbientSample + i;
			ArcLightCube lightCube = ambientLightCubes[sampleId];
			float dist = Vectors.distanceSquared(lightCube.getPosition(leaf), position);
			
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
		if (bsp == null || bsp.leaves == null)
			return;
		
		bsp.cleanUp();
		
		final Texture[] textures = textureData.getTextures();
		for(Texture texture : textures)  {
			if (texture != Resources.DEFAULT)
				texture.cleanUp();
		}
	
		for(Model model : models) {
			for(Mesh mesh : model.getMeshes()) {
				mesh.cleanUp();
			}
		}
		
		if (environmentMaps != null) {
			for(Texture texture : environmentMaps.values()) {
				if (texture != Resources.DEFAULT)
					texture.cleanUp();
			}
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
		funcHandler.callCommand(caller.position, cmd);
	}
	
	public void callCommand(String cmd) {
		funcHandler.callCommand(Vectors.ZERO, cmd);
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
	 * @param position the ray's origin
	 * @param dir the ray's direction
	 * @return the distance the ray traveled and face, or null if nothing was hit
	 */
	public BspRaycast raycast(Vector3f org, Vector3f dir) {
		float shortestDist = Float.POSITIVE_INFINITY;
		ArcFace collidedFace = null;

		activeLeaves.beginIteration();
		while(activeLeaves.hasNext()) {
			BspLeaf leaf = activeLeaves.next();
			
			Vector3f bounds = Vectors.sub(leaf.max, leaf.min).div(2f);
			Vector3f center = Vectors.add(leaf.min, bounds);
			
			float distLeaf = new AABB(center, bounds).collide(org, dir);
			
			if (Float.isInfinite(distLeaf))
				continue;
			
			if (distLeaf < shortestDist) {
				
				ArcFace[] faces = bsp.getFaces(leaf);
				for(ArcFace face : faces) {
					
					if (face.texMapping == -1) continue;
					
					float dist = CollideUtils.raycastMapGeometry(bsp, face, org, dir);
					if (!Float.isNaN(dist) && dist < shortestDist) {
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

	public List<BspLeaf> getVisibleLeavesIntersecting(AABB box) {
		Vector3f max = Vectors.add(box.getCenter(), box.getBounds());
		Vector3f min = Vectors.sub(box.getCenter(), box.getBounds());
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
			
			float dist = Vectors.distanceSquared(clip.center, position);
			
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
	
	public RailList[] getRailList() {
		return railList;
	}

	public void setRailList(RailList[] railList) {
		this.railList = railList;
	}
	
	private void addBlockId(List<Integer> blockIds, float x, float z) {
		int dx = (int)(x - bsp.min.x) / RailList.BLOCK_SIZE;
		int dz = (int)(z - bsp.min.z) / RailList.BLOCK_SIZE;
		
		//Vector3f xxx = new Vector3f((bsp.min.x+32) + (float)Math.floor((x - bsp.min.x)/64f)*64f,0,(bsp.min.z+32) + (float)Math.floor((z - bsp.min.z)/64f)*64f);
		//Vector3f yyy = new Vector3f(32,600,32);
		//LineRender.drawBox(xxx, yyy, Colors.PINK);
		
		int blockId = dx + (dz * RailList.numBlocksX);
		
		if (blockId >= 0 && blockId < RailList.numBlocks && !blockIds.contains(blockId))
			blockIds.add(blockId);
	}

	public Rail getNearestRail(Vector3f pos, Vector3f dir, Rail exception, float bounds, float gravitation) {
		
		List<Integer> blockIds = new ArrayList<>();
		
		addBlockId(blockIds, pos.x - bounds, pos.z - bounds);
		addBlockId(blockIds, pos.x + bounds, pos.z - bounds);
		addBlockId(blockIds, pos.x - bounds, pos.z + bounds);
		addBlockId(blockIds, pos.x + bounds, pos.z + bounds);
		
		Vector3f dirNorm = new Vector3f(dir).normalize();
		
		Rail bestRail = null;
		float shorestLen = gravitation;
		
		for(int blockId : blockIds) {
			if (railList[blockId] == null)
				continue;
			
			List<Rail> rails = railList[blockId].getRails();
			
			if (rails == null)
				return null;
			
			for(Rail rail : rails) {
				if (rail == exception)
					continue;
				
				float dist = GeomUtil.pointDistanceToEdge(pos, rail.getStart(), rail.getEnd());

				// Handle endpoints
				if (Float.isNaN(dist)) {
					float distToStart = Math.min(Vectors.distanceSquared(pos, rail.getStart()),
							Vectors.distanceSquared(pos, rail.getEnd()));

					if (distToStart < gravitation * gravitation)
						dist = (float)Math.sqrt(distToStart);
				}
				
				// Weigh towards rails matching our dir
				Vector3f railNormal = Vectors.sub(rail.getEnd(), rail.getStart()).normalize();
				
				dist += (1f - Math.abs(dirNorm.dot(railNormal)));
	
				if (dist < shorestLen) {
					shorestLen = dist;
					bestRail = rail;
				}
			}
		}
		
		return bestRail;
	}

	public Rail findLinkingRail(Rail target, Vector3f pos, float bounds) {
		
		List<Integer> blockIds = new ArrayList<>();
		
		addBlockId(blockIds, pos.x - bounds, pos.z - bounds);
		addBlockId(blockIds, pos.x + bounds, pos.z - bounds);
		addBlockId(blockIds, pos.x - bounds, pos.z + bounds);
		addBlockId(blockIds, pos.x + bounds, pos.z + bounds);
		
		float nearestRail = 1f;
		Rail targetRail = null;
		
		for(int blockId : blockIds) {
			RailList list = railList[blockId];
			
			if (list == null)
				continue;
			List<Rail> rails = list.getRails();

			for(Rail rail : rails) {
				if (rail == target)
					continue;
				
				float d1 = Vectors.distanceSquared(pos, rail.getStart());
				float d2 = Vectors.distanceSquared(pos, rail.getEnd());
				
				float dist = Math.min(d1, d2);
				
				if (dist <= nearestRail) {
					nearestRail = dist;
					targetRail = rail;
				}
			}
		}
		return targetRail;
	}

	public void drawRails(Vector3f pos, Rail exception, float bounds) {

		List<Integer> blockIds = new ArrayList<>();

		addBlockId(blockIds, pos.x - bounds, pos.z - bounds);
		addBlockId(blockIds, pos.x + bounds, pos.z - bounds);
		addBlockId(blockIds, pos.x - bounds, pos.z + bounds);
		addBlockId(blockIds, pos.x + bounds, pos.z + bounds);

		for (int blockId : blockIds) {
			RailList list = railList[blockId];
			
			if (list == null)
				continue;
			
			List<Rail> rails = list.getRails();

			for (Rail rail : rails) {
				LineRender.drawLine(rail.getStart(), rail.getEnd(), (rail == exception) ? Colors.RED : Colors.BLUE);
			}
		}
	}
}
