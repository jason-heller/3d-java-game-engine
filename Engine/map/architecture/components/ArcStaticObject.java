package map.architecture.components;

import org.joml.Vector3f;

import geom.AxisAlignedBBox;
import gl.res.Model;

public class ArcStaticObject {

	public Vector3f pos;
	public Vector3f rot;
	public Vector3f lightingPos;
	public short objLeafResIndex;
	public byte numObjLeafRes;
	public short model;
	public byte solidity;
	public float visRange;
	
	private AxisAlignedBBox bbox;
	
	public void setBBox(Model model) {
		Vector3f center = new Vector3f(pos);
		center.y += model.bounds.y;
		bbox = new AxisAlignedBBox(center, model.bounds);
	}
	
	public AxisAlignedBBox getBBox() {
		return bbox;
	}
}
