package map.ground;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL15;

import gl.res.Vbo;

public class ChunkMeshHandler {
	public static final int MIN_POLY_SIZE = 8; // Must be a power of 2
	public static final int STRIPE_SIZE = 17;
	public static final int NUM_QUADS_PER_STRIPE = STRIPE_SIZE - 1;
	private static final int NUM_VERTICES = STRIPE_SIZE * STRIPE_SIZE;
	public static final int TESSELATION_RATIO = (Terrain.MAP_SIZE_UNITS / NUM_QUADS_PER_STRIPE); 
	
	public static Vbo[] vertexVbos;
	public static Vbo textureCoordVbo;
	public static Vbo indexVbo;
	
	public static int indexCount;
	
	public ChunkMeshHandler() {
		// Load LODs
		// Determine number of LODs
		
		textureCoordVbo = createTexCoordVbo();
		indexVbo = createIndexVbo();
		
		int subdiv = Terrain.MAP_SIZE_UNITS;
		int count = 0;
		while(subdiv > NUM_QUADS_PER_STRIPE) {		// Theres definitely a simpler way to do this
			subdiv /= 2;
			count++;
		}
		vertexVbos = new Vbo[count];
		
		int scale = 1;
		final int size = Terrain.MAP_SIZE_UNITS / NUM_QUADS_PER_STRIPE / MIN_POLY_SIZE;
		for(int i = 0; i < vertexVbos.length; i++) {
			vertexVbos[i] = createVertexVbo(size / scale);
			scale *= 2;
		}
	}

	public void cleanUp() {
		for(Vbo vbo: vertexVbos) {
			vbo.delete();
		}
		
		textureCoordVbo.delete();
		indexVbo.delete();
	}

	private Vbo createVertexVbo(float scale) {
		float[] vertices = new float[3 * NUM_VERTICES];
		int i = 0;
		for(int z = 0; z < STRIPE_SIZE; z++) {
			for(int x = 0; x < STRIPE_SIZE; x++) {
				vertices[i++] = x * scale * MIN_POLY_SIZE;
				vertices[i++] = 0f;
				vertices[i++] = z * scale * MIN_POLY_SIZE;
			}
		}

		final Vbo vbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbo.bind();
		vbo.storeData(vertices);
		vbo.unbind();
		return vbo;
	}
	
	private static Vbo createTexCoordVbo() {
		float[] uvs = new float[2 * NUM_VERTICES];
		int i = 0;
		int tx = 0, tz = 0;
		for(int z = 0; z < STRIPE_SIZE; z++) {
			tx = 0;
			for(int x = 0; x < STRIPE_SIZE; x++) {
				uvs[i++] = tx;
				uvs[i++] = tz;
				tx ^= 1;
				
			}
			tz ^= 1;
		}

		final Vbo vbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbo.bind();
		vbo.storeData(uvs);
		vbo.unbind();
		return vbo;
	}
	
	private static Vbo createIndexVbo() {
		final int stripeMinusOne = (STRIPE_SIZE - 1);
		final int quadCount = stripeMinusOne * stripeMinusOne;
		indexCount = 6 * quadCount;
		
		int[] inds = new int[indexCount];
		int rel = 0;
		int i = 0;

		for (int x = 0; x < stripeMinusOne; x++) {
			for (int z = 0; z < stripeMinusOne; z++) {
				inds[i + 0] = rel + 1;
				inds[i + 1] = rel + 0;
				inds[i + 2] = rel + STRIPE_SIZE + 1;
				inds[i + 3] = rel + STRIPE_SIZE + 1;
				inds[i + 4] = rel + 0;
				inds[i + 5] = rel + STRIPE_SIZE;
				rel++;
				i += 6;
			}
			rel++;
		}

		final Vbo vbo = Vbo.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
		vbo.bind();
		vbo.storeData(inds);
		vbo.unbind();
		return vbo;
	}

	public static Vbo buildHeightNormalVbo(int x, int z, int tesselation, Vector4f[][] heights) {
		float[] h = new float[heights.length * heights[0].length * 4];
		int index = 0;
		for(int terrainX = 0; terrainX < STRIPE_SIZE; terrainX++) {
			for(int terrainZ = 0; terrainZ < STRIPE_SIZE; terrainZ++) {
				h[index++] = heights[terrainZ][terrainX].x;
				h[index++] = heights[terrainZ][terrainX].y;
				h[index++] = heights[terrainZ][terrainX].z;
				h[index++] = heights[terrainZ][terrainX].w;
			}
		}
		final Vbo vbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbo.bind();
		vbo.storeData(h);
		vbo.unbind();
		return vbo;
	}
}
