package map.architecture.components;

import org.joml.Vector3f;

import map.architecture.vis.BspLeaf;

public class ArcLightCube {

	public static final Vector3f[] NO_LIGHT = new Vector3f[] { Vector3f.ZERO, Vector3f.ZERO, Vector3f.ZERO,
			Vector3f.ZERO, Vector3f.ZERO, Vector3f.ZERO };

	public float x, y, z;
	public int[] colors;
	
	public Vector3f applyLighting(Vector3f direction) {
		float ax = Math.abs(direction.x), ay = Math.abs(direction.y), az = Math.abs(direction.z);
		int index = 0;
		if (ax > ay && ax > az) {
			index = Math.signum(direction.x) > 0 ? 0 : 1;
		} else if (ay > ax && ay > az) {
			index = Math.signum(direction.y) > 0 ? 2 : 3;
		} else {
			index = Math.signum(direction.z) > 0 ? 4 : 5;
		}
		
		return getColor(index);
	}
	
	public Vector3f[] getLighting() {
		Vector3f[] lights = new Vector3f[6];
		for(int i = 0; i < 6; i++) {
			lights[i] = getColor(i);
		}
		return lights;
	}

	public Vector3f getColor(int index) {
		
		float exp = (float) Math.pow(2, (byte)(colors[index] >>> 24));
		float r = ((colors[index] >>> 0 ) & 0xFF) * 8.0313725499f;
		float g = ((colors[index] >>> 8 ) & 0xFF) * 8.0313725499f;
		float b = ((colors[index] >>> 16) & 0xFF) * 8.0313725499f;

		return new Vector3f(r * exp, g * exp, b * exp);
	}

	public Vector3f getPosition(BspLeaf leaf) {
		final Vector3f bounds = Vector3f.sub(leaf.max, leaf.min);
		final Vector3f ratio = new Vector3f(x, y, z);
		final Vector3f lightPosition = Vector3f.add(leaf.min, Vector3f.mul(ratio, bounds));
		return lightPosition;
	}

}
