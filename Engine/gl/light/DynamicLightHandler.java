package gl.light;

import org.joml.Vector3f;

public class DynamicLightHandler {
	public static final int MAX_DYNAMIC_LIGHTS = 4;
	
	private int lightPtr = 0;
	private DynamicLight[] lights;
	
	public DynamicLightHandler() {
		lights = new DynamicLight[MAX_DYNAMIC_LIGHTS];
	}
	
	public DynamicLight addLight(Vector3f pos, Vector3f dir, float strength) {
		if (lightPtr == MAX_DYNAMIC_LIGHTS) {
			return null;
		}
		
		DynamicLight light = new DynamicLight(pos, dir, strength, lightPtr);

		lights[lightPtr++] = light;
		return light;
	}
	
	public void removeLight(DynamicLight light) {
		boolean found = false;
		for(int i = 0; i < MAX_DYNAMIC_LIGHTS; i++) {
			if (found) {
				lights[i-1] = lights[i];
				lights[i] = null;
			} else {
				if (light == lights[i]) {
					found = true;
					lights[i] = null;		// If its the last index
					lightPtr--;
				}
			}
		}
	}

	public DynamicLight[] getLights() {
		return lights;
	}
}
