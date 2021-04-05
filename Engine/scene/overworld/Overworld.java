package scene.overworld;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import dev.Debug;
import gl.Camera;
import map.Enviroment;
import scene.PlayableScene;
import scene.entity.util.PlayerEntity;

public class Overworld extends PlayableScene {
	
	private Enviroment env;
	
	public Overworld() {
		super();
		Application.scene = this;		// Hack
		Resources.addTexture("grass", "tex/TEST_GRASS.png");
		camera.setPosition(new Vector3f(0,10,0));

		player = new PlayerEntity(camera);
		
		arcHandler.load(this, new Vector3f(), "test");
		env = new Enviroment(this, new Vector3f(1,1,1));
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		env.cleanUp();
		arcHandler.cleanUp();
		Resources.removeTexture("grass");
	}

	@Override
	public Camera getCamera() {
		return camera;
	}
	
	@Override
	public void update() {
		super.update();
		if (ui.isPaused()) return;
		
		env.update(this);
		arcHandler.update(camera);
		
		if (isLoading) {
			if (env.isFullyLoaded()) {
				isLoading = false;
			}
			return;
		}
		
	}

	@Override
	public void render(float clipX, float clipY, float clipZ, float clipDist) {
		env.render(camera);
		arcHandler.render(camera, clipX, clipY, clipZ, clipDist);
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
		
	}

	public Enviroment getEnviroment() {
		return env;
	}

}
