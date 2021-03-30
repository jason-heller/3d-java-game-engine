package scene.singlearc;

import org.joml.Vector3f;

import core.Application;
import dev.Debug;
import gl.Camera;
import gl.skybox.Skybox2D;
import scene.PlayableScene;
import scene.entity.utility.PlayerEntity;

public class SingleArcScene extends PlayableScene {
	
	private Skybox2D skybox;
	
	public SingleArcScene() {
		super();
		Application.scene = this;		// Hack to make the variable update before constructors runs
		camera.setPosition(new Vector3f(0,10,0));

		player = new PlayerEntity(camera);
		
		arcHandler.load(this, new Vector3f(), PlayableScene.currentMap);
		skybox = new Skybox2D();
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		skybox.cleanUp();
	}

	@Override
	public Camera getCamera() {
		return camera;
	}
	
	@Override
	public void update() {
		super.update();
		if (ui.isPaused()) return;
		if (isLoading) {
			isLoading = false;
		}
		
	}

	@Override
	public void render() {
		super.render();
		skybox.render(camera, Vector3f.Y_AXIS);
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
		
	}

}
