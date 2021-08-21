package gl.skybox;

import gl.Camera;
import map.architecture.Architecture;

public interface Skybox {
	public void render(Architecture arc, Camera camera);
	public void cleanUp();
}
