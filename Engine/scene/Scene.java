package scene;

import org.joml.Vector4f;

import gl.Camera;

public interface Scene {
	public void update();
	public void tick();
	public void cleanUp();
	public Camera getCamera();
	public void renderNoReflect();
	public void render(Vector4f clipPlane);
	public void postRender();
	public void fastRender(Vector4f vector4f);
}
