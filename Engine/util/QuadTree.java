package util;

import java.util.ArrayList;
import java.util.List;

public class QuadTree<Value>  {
	
	private List<Node> nodes;
	
	private QuadTree<Value> northWest = null;
	private QuadTree<Value> northEast = null;
	private QuadTree<Value> southWest = null;
	private QuadTree<Value> southEast = null;
	
	private int depth;
	private Boundry boundry;

	public QuadTree(int width, int height) {
		this(0, 0, width, height);
	}
	
	public QuadTree(int x, int y, int width, int height) {
		this(0, x, y, width, height);
	}
	
	private QuadTree(int depth, int x, int y, int width, int height) {
		nodes = new ArrayList<Node>();
		this.boundry = new Boundry(x, y, width, height);
		this.depth = depth;
	}

	
	public void depthFirst(int x, int y) {
		depthFirst(x, y, this);
	}
	
	private List<Node> depthFirst(int x, int y, QuadTree<Value> tree) {
		if (tree.northWest.boundry.inRange(x, y))
			return depthFirst(x, y, tree.northWest);
		if (tree.northEast.boundry.inRange(x, y))
			return depthFirst(x, y, tree.northEast);
		if (tree.southWest.boundry.inRange(x, y))
			return depthFirst(x, y, tree.southWest);
		if (tree.southEast.boundry.inRange(x, y))
			return depthFirst(x, y, tree.southEast);
		
		return this.nodes;
	}

	private void split() {
		int xOffset = this.boundry.xMin + (this.boundry.xMax - this.boundry.xMin) / 2;
		int yOffset = this.boundry.yMin + (this.boundry.yMax - this.boundry.yMin) / 2;

		northWest = new QuadTree<Value>(this.depth + 1, this.boundry.xMin, this.boundry.yMin, xOffset, yOffset);
		northEast = new QuadTree<Value>(this.depth + 1, xOffset, this.boundry.yMin, this.boundry.xMax, yOffset);
		southWest = new QuadTree<Value>(this.depth + 1, this.boundry.xMin, yOffset, xOffset, this.boundry.yMax);
		southEast = new QuadTree<Value>(this.depth + 1, xOffset, yOffset, this.boundry.xMax, this.boundry.yMax);

	}

	public void insert(int x, int y, Value value) {
		if (!this.boundry.inRange(x, y)) {
			return;
		}

		Node node = new Node(x, y, value);
		if (nodes.size() < 4) {
			nodes.add(node);
			return;
		}

		if (northWest == null) {
			split();
		}

		if (northWest.boundry.inRange(x, y))
			northWest.insert(x, y, value);
		else if (northEast.boundry.inRange(x, y))
			northEast.insert(x, y, value);
		else if (southWest.boundry.inRange(x, y))
			southWest.insert(x, y, value);
		else if (southEast.boundry.inRange(x, y))
			southEast.insert(x, y, value);
		else
			System.out.printf("ERROR : Unhandled partition %d %d", x, y);
	}
	
	private class Node {
		public int x, y;
		public Value value;

		Node(int x, int y, Value value) {
			this.x = x;
			this.y = y;
			this.value = value;
		}
	}

	private class Boundry {
		public int xMin, yMin, xMax, yMax;
		
		public Boundry(int xMin, int yMin, int xMax, int yMax) {
			super();
			this.xMin = xMin;
			this.yMin = yMin;
			this.xMax = xMax;
			this.yMax = yMax;
		}

		public boolean inRange(int x, int y) {
			return (x >= xMin && x <= xMax && y >= yMin && y <= yMax);
		}
	}
}