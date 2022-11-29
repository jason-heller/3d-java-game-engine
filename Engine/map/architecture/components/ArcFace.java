package map.architecture.components;

public class ArcFace {
	public int lmIndex;
	 
	public float[] lmMins;
	public float[] lmSizes;
	public byte[] lmStyles;
	// public byte onNode;
	public short planeId;
	public int firstEdge;
	public short numEdges;
	public short texMapping;

	public float[] lightmapOffsetX, lightmapOffsetY;
	public float lightmapScaleX, lightmapScaleY;
}
