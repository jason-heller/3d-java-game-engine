package map.ground;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.joml.Vector3f;

import gl.Camera;

public class Terrain {
	
	private DistQuadTree lodTree;
	private HashMap<Key, Chunk> chunks;
	private List<Chunk> chunkLoadQueue;
	private ChunkMeshHandler cmHandler;
	private TerrainSampler tSampler;
	
	public static int MAP_SIZE_UNITS = -1;
	private static final int MIN_CHUNK_SIZE = ChunkMeshHandler.MIN_POLY_SIZE * (ChunkMeshHandler.NUM_QUADS_PER_STRIPE);
	
	public static int size = 3;
	private int lastX, lastZ;
	
	public Terrain(Camera camera, String heightmapFile, int quadToPixelScale) {
		tSampler = new TerrainSampler(heightmapFile, quadToPixelScale);
		cmHandler = new ChunkMeshHandler();
		
		chunks = new HashMap<Key, Chunk>();
		chunkLoadQueue = new ArrayList<Chunk>();
		
		Vector3f pos = camera.getPosition();
		lastX = (int)pos.x / MIN_CHUNK_SIZE;
		lastZ = (int)pos.z / MIN_CHUNK_SIZE;
		final int halfSize = (MAP_SIZE_UNITS / 2);
		lodTree = new DistQuadTree(-halfSize, -halfSize, halfSize, halfSize);
		lodTree.buildTree(camera);
		lodTree.depthFirst(this);
	}

	public void update(Camera camera) {
		tSampler.update();
		
		Vector3f pos = camera.getPosition();
		int x = (int)pos.x / MIN_CHUNK_SIZE;
		int z = (int)pos.z / MIN_CHUNK_SIZE;
		if (x != lastX || z != lastZ) {
			lastX = x;
			lastZ = z;
			lodTree.buildTree(camera);
			lodTree.depthFirst(this);
		}
	}
	
	public void requestChunk(Boundary boundary) {

		int tesselation = boundary.getTesselation();
		Key key = new Key(boundary.xMin, boundary.yMin);
		/*Chunk chunk = chunks.get(key);
		if (chunk != null) {
			if (chunk.getTesselation() != tesselation) {
				// Delete old chunk
				//toBeReplaced.put(chunk, c)
				//chunks.remove(key);
				queueChunkLoad(boundary);
			}
			else {
				// Retain chunk
				//chunkLoadQueue.add(chunk);
				queueChunkLoad(boundary);
			}
		}
		else if (chunk == null) {
			queueChunkLoad(boundary);
		}*/
		queueChunkLoad(boundary);
	}
	
	private void queueChunkLoad(Boundary boundary) {
		Chunk chunk = new Chunk(boundary.xMin, boundary.yMin, boundary.tesselation);
		tSampler.requestHeights(chunk);
		chunkLoadQueue.add(chunk);
	}
	
	// Called after the new quadtree is done 
	public void cleanChunks() {
		for(Chunk chunk : chunks.values()) {
			tSampler.queueForRemoval(chunk);
		}
		
		chunks.clear();
		
		for(Chunk chunk : chunkLoadQueue) {
			chunks.put(new Key(chunk.x, chunk.z), chunk);
		}
		
		chunkLoadQueue.clear();
	}
	
	public void cleanUp() {
		for(Chunk chunk : chunks.values()) {
			chunk.cleanUp();
		}
		
		cmHandler.cleanUp();
	}
	
	private class Key {

		public final int x;
		public final int y;

		public Key(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public boolean equals(final Object O) {
			if (!(O instanceof Key))
				return false;
			if (((Key) O).x != x)
				return false;
			if (((Key) O).y != y)
				return false;
			return true;
		}

		public int hashCode() {
			return (x << 16) + y;
			//return y > x ? y * y + x : x * x + x + y;
		}

	}
	
	public Chunk getChunk(int x, int z) {
		Boundary boundary = this.lodTree.depthFirst(x, z);
		return chunks.get(new Key(boundary.xMin, boundary.yMin));
	}

	public Collection<Chunk> getChunks() {
		return chunks.values();
	}

	public Collection<Chunk> getPrevChunks() {
		return tSampler.getOldChunks();
	}

	public boolean isFullyLoaded() {
		return tSampler.isFullyLoaded();
	}

}
