package scene;

import gl.Camera;

public interface Scene {
	public void update();
	public void tick();
	public void cleanUp();
	public Camera getCamera();
	public void renderNoReflect();
	public void render(float clipX, float clipY, float clipZ, float clipDist);
}
