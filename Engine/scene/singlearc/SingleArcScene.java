package scene.singlearc;

import org.joml.Vector3f;

import core.Application;
import dev.Console;
import dev.Debug;
import gl.Camera;
import gl.skybox.Skybox2D;
import io.Input;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;

public class SingleArcScene extends PlayableScene {
	
	private Skybox2D skybox;
	
	public SingleArcScene() {
		super();
		Application.scene = this;		// Hack to make the variable update before constructors runs
		camera.setPosition(new Vector3f(0,10,0));
		
		player = new PlayerEntity(camera);
		arcHandler.load(this, new Vector3f(), PlayableScene.currentMap);
		EntityHandler.addEntity(player);
		arcHandler.getArchitecture().callCommand("spawn_player");
		
		if (arcHandler.isSkyboxEnabled())
			skybox = new Skybox2D();
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		if (arcHandler.isSkyboxEnabled())
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
		
		if (PlayerEntity.getHp(0) <= 0 && Camera.offsetY < PlayerHandler.CAMERA_STANDING_HEIGHT - .2f) {
			int key = Input.getAny();
			
			if (key == 0 || Console.isVisible()) {
				return;
			}
			
			player.reset();
			Console.send("map "+PlayableScene.currentMap);
		}
		
	}

	@Override
	public void render(float clipX, float clipY, float clipZ, float clipDist) {
		super.render(clipX, clipY, clipZ, clipDist);
		if (arcHandler.isSkyboxEnabled())
			skybox.render(camera, Vector3f.Y_AXIS);
	}

}
