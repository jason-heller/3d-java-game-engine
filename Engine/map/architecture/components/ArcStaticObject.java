package map.architecture.components;

import org.joml.Vector3f;

import geom.BoundingBox;
import gl.res.Mesh;

public class ArcStaticObject {

	public Vector3f pos;
	public Vector3f rot;
	public Vector3f lightingPos;
	public short objLeafResIndex;
	public byte numObjLeafRes;
	public short model;
	public byte solidity;
	public float visRange;
	
	private BoundingBox bbox;
	
	public void setBBox(Mesh model) {
		Vector3f center = new Vector3f(pos);
		center.y += model.bounds.y;
		bbox = new BoundingBox(center, model.bounds);
	}
	
	public BoundingBox getBBox() {
		return bbox;
	}
}
