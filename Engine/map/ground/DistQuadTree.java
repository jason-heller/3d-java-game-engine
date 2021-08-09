package map.ground;

import org.joml.Vector3f;

import gl.Camera;

public class DistQuadTree {

	private static final int MIN_QUAD_SIZE_X2 = ChunkMeshHandler.MIN_POLY_SIZE * (ChunkMeshHandler.NUM_QUADS_PER_STRIPE) * 2;
	private static final float WID_MULTIPLIER = 5f; // 6 is a good number
	private DistQuadTree northWest = null;
	private DistQuadTree northEast = null;
	private DistQuadTree southWest = null;
	private DistQuadTree southEast = null;

	private int depth;
	private Boundary boundary;

	DistQuadTree(int x2, int y2) {
		this(0, 0, x2, y2);
	}

	DistQuadTree(int x1, int y1, int x2, int y2) {
		this(0, x1, y1, x2, y2);
	}

	private DistQuadTree(int depth, int x1, int y1, int x2, int y2) {
		this.boundary = new Boundary(x1, y1, x2, y2, depth);
		this.depth = depth;
	}

	public void depthFirst(Terrain terrain) {
		depthFirst(terrain, this);

		terrain.cleanChunks();
	}

	private void depthFirst(Terrain terrain, DistQuadTree tree) {
		if (tree.northEast == null) {
			// Pass boundary to terrain
			terrain.requestChunk(tree.boundary);
			return;
		}

		depthFirst(terrain, tree.northWest);
		depthFirst(terrain, tree.northEast);
		depthFirst(terrain, tree.southWest);
		depthFirst(terrain, tree.southEast);
	}
	
	public Boundary depthFirst(int x, int y) {
		return depthFirst(x, y, this);
	}
	
	private Boundary depthFirst(int x, int y, DistQuadTree tree) {
		if (tree.northWest.boundary.inRange(x, y))
			return depthFirst(x, y, tree.northWest);
		if (tree.northEast.boundary.inRange(x, y))
			return depthFirst(x, y, tree.northEast);
		if (tree.southWest.boundary.inRange(x, y))
			return depthFirst(x, y, tree.southWest);
		if (tree.southEast.boundary.inRange(x, y))
			return depthFirst(x, y, tree.southEast);
		
		return this.boundary;
	}

	private void split() {
		int xOffset = this.boundary.xMin + ((this.boundary.xMax - this.boundary.xMin) / 2);
		int yOffset = this.boundary.yMin + ((this.boundary.yMax - this.boundary.yMin) / 2);

		northWest = new DistQuadTree(this.depth + 1, this.boundary.xMin, this.boundary.yMin, xOffset, yOffset);
		northEast = new DistQuadTree(this.depth + 1, xOffset, this.boundary.yMin, this.boundary.xMax, yOffset);
		southWest = new DistQuadTree(this.depth + 1, this.boundary.xMin, yOffset, xOffset, this.boundary.yMax);
		southEast = new DistQuadTree(this.depth + 1, xOffset, yOffset, this.boundary.xMax, this.boundary.yMax);

	}

	public void buildTree(Camera camera) {
		final int width = (boundary.xMax - boundary.xMin);
		final int height = (boundary.yMax - boundary.yMin);
		
		if (width < MIN_QUAD_SIZE_X2)
			return;

		Vector3f pos = camera.getPosition();

		float centerX = boundary.xMin + (width / 2f);
		float centerZ = boundary.yMin + (height / 2f);

		float dx = pos.x - centerX;
		float dz = pos.z - centerZ;

		float distSqr = (dx * dx) + (dz * dz);
		
		if (distSqr < (width * width) * WID_MULTIPLIER) {
			split();
			northWest.buildTree(camera);
			northEast.buildTree(camera);
			southWest.buildTree(camera);
			southEast.buildTree(camera);
		}
	}
}