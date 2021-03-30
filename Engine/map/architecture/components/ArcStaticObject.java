package map.architecture.components;

import org.joml.Vector3f;

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
	
	public Model getModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
