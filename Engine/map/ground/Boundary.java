package map.ground;

class Boundary {
	public int xMin, yMin, xMax, yMax, tesselation;
	
	public Boundary(int xMin, int yMin, int xMax, int yMax, int tesselation) {
		super();
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.tesselation = tesselation;
	}

	public boolean inRange(int x, int y) {
		return (x >= xMin && x <= xMax && y >= yMin && y <= yMax);
	}

	public int getTesselation() {
		return tesselation;
	}
}