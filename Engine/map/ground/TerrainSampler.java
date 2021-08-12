package map.ground;

import static map.ground.ChunkMeshHandler.MIN_POLY_SIZE;
import static map.ground.ChunkMeshHandler.STRIPE_SIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joml.Vector3f;
import org.joml.Vector4f;

import core.Resources;
import dev.cmd.Console;
import util.MathUtil;

public class TerrainSampler {

	private static int[][] heightmap;
	private static Map<Chunk, Future<Vector4f[][]>> futures = new HashMap<>();
	private static List<Chunk> toBeRemoved = new ArrayList<>();

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	public static int MAP_SIZE_SAMPLES = -1;
	private static int quadToPixelScale = -1;

	public TerrainSampler(String heightmapFile, int quadToPixelScale) {
		TerrainSampler.quadToPixelScale = quadToPixelScale;
		loadHeightmap(heightmapFile);
	}

	private void loadHeightmap(String heightmapFile) {
		final float[][][] data = Resources.addHeightmapByTexture(heightmapFile);
		Terrain.MAP_SIZE_UNITS = data.length * ChunkMeshHandler.MIN_POLY_SIZE;
		MAP_SIZE_SAMPLES = data.length;
		
		final int len = MAP_SIZE_SAMPLES;
		heightmap = new int[(len / 4) + 2][len + 2];
		

		if (data.length < len) {
			Console.log("#rERROR: Heightmap size is smaller than expected size. Failed to load height data.");
			Console.log("#rHeightmap size: " + data.length, "Expected size: " + len);
			return;
		}

		else if (data.length != len) {
			Console.log(
					"#yWarning: Heightmap is bigger than expected size. Heightmap may not be correct or is loading unused data.");
			Console.log("#rHeightmap size: " + data.length, "Expected size: " + len);
		}

		for (int i = 0; i < len; i += 4) {
			for (int j = 0; j < len; j++) {
				int val = (int) (255 * data[i][j][0]);
				val = val << 8 | (int) (255 * data[i + 1][j][0]);
				val = val << 8 | (int) (255 * data[i + 2][j][0]);
				val = val << 8 | (int) (255 * data[i + 3][j][0]);
				heightmap[(i / 4) + 1][j + 1] = val;
			}
		}
	}

	public void update() {
		Iterator<Chunk> iter = futures.keySet().iterator();

		if (!iter.hasNext()) {
			refresh();
		}

		while (iter.hasNext()) {
			Chunk chunk = iter.next();
			Future<Vector4f[][]> future = futures.get(chunk);
			if (future.isDone()) {
				try {
					chunk.addHeights(future.get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				iter.remove();
			}
			// futures.remove(chunk);
		}
	}

	public void refresh() {
		if (futures.keySet().size() == 0) {
			for (Chunk chunk : toBeRemoved) {
				chunk.cleanUp();
			}

			toBeRemoved.clear();
		}
	}

	public void requestHeights(Chunk chunk) {
		final Future<Vector4f[][]> future = executor.submit(new HeightCallable(chunk.x, chunk.z, chunk.getTesselation()));
		futures.put(chunk, future);
	}

	private static Vector4f[][] calcHeights(int x, int z, int tesselation) {

		final int arrLen = STRIPE_SIZE;
		Vector4f[][] heights = new Vector4f[arrLen][arrLen];
		final int scale = ChunkMeshHandler.TESSELATION_RATIO / (int) Math.pow(2, tesselation);
		for (int terrainX = 0; terrainX < arrLen; terrainX++) {
			for (int terrainZ = 0; terrainZ < arrLen; terrainZ++) {
				int ix = (x + (terrainX * scale));
				int iz = (z + (terrainZ * scale));
				
				// Create normal based on heights
				final float topRight = heightAt(ix + MIN_POLY_SIZE, iz);
				final float topLeft  = heightAt(ix, 				iz);
				// final float btmRight = heightAt(ix, 				iz + MIN_POLY_SIZE);
				final float btmLeft  = heightAt(ix + MIN_POLY_SIZE, iz + MIN_POLY_SIZE);
				
				Vector3f normal = Vector3f.cross(new Vector3f(MIN_POLY_SIZE, topRight - topLeft, 0),
						new Vector3f(0, btmLeft - topLeft, MIN_POLY_SIZE));
				normal.normalize().negate();
				// ------------------------------
						
				heights[terrainX][terrainZ] = new Vector4f(topLeft, normal.x, normal.y, normal.z);
			}
		}

		return heights;
	}
	
	public static float barycentric(float x, float z) {

		float modX = (((x % MIN_POLY_SIZE) + MIN_POLY_SIZE) % MIN_POLY_SIZE);
		float modZ = (((z % MIN_POLY_SIZE) + MIN_POLY_SIZE) % MIN_POLY_SIZE);

		int topLeftX = (int)Math.floor(x / MIN_POLY_SIZE) * MIN_POLY_SIZE;
		int topLeftZ = (int)Math.floor(z / MIN_POLY_SIZE) * MIN_POLY_SIZE;
		
		Vector3f p1, p2, p3;
		p1 = new Vector3f(0, heightAt(topLeftX, topLeftZ), 0);
		p2 = new Vector3f(MIN_POLY_SIZE, heightAt(topLeftX + MIN_POLY_SIZE, topLeftZ + MIN_POLY_SIZE), MIN_POLY_SIZE);
		
		if (modZ > modX) {
			// In lower left sector of quad
			p3 = new Vector3f(0, heightAt(topLeftX, topLeftZ + MIN_POLY_SIZE), MIN_POLY_SIZE);
			return MathUtil.barycentric(modX, modZ, p1, p3, p2);
		}
		// In top right
		p3 = new Vector3f(MIN_POLY_SIZE, heightAt(topLeftX + MIN_POLY_SIZE, topLeftZ), 0);
		return MathUtil.barycentric(modX, modZ, p1, p2, p3);
	}
	
	/** Gets the height at position x,z (NOTE: x, z must be divisible by 2)
	 * @param x the x position, divisible by 2
	 * @param z the z position, divisible by 2
	 * @return The height in the heightmap
	 */
	public static int heightAt(int x, int z) {
		//x = Math.floorDiv(x, quadToPixelScale) * quadToPixelScale;
		//z = Math.floorDiv(z, quadToPixelScale) * quadToPixelScale;
		final int halfSamples = (MAP_SIZE_SAMPLES) / 2;
		int ix = (x / MIN_POLY_SIZE) + halfSamples;
		int iz = (z / MIN_POLY_SIZE) + halfSamples;

		int bitShift = (3 - (ix % 4)) * 8;	// 8 = num bits in a byte, 4 bytes in an int

		return ((heightmap[(ix / 4)][iz] >> bitShift) & 0xFF) * 3;
	}

	private static class HeightCallable implements Callable<Vector4f[][]> {

		int x, z, tesselation;

		public HeightCallable(int x, int z, int tesselation) {
			this.x = x;
			this.z = z;
			this.tesselation = tesselation;
		}

		@Override
		public Vector4f[][] call() throws Exception {
			return calcHeights(x, z, tesselation);
		}

	}

	public void queueForRemoval(Chunk chunk) {
		toBeRemoved.add(chunk);
	}

	public List<Chunk> getOldChunks() {
		return toBeRemoved;
	}

	public boolean isFullyLoaded() {
		return futures.isEmpty();
	}
}
