package scene.mapscene;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import core.Application;
import dev.Console;
import gl.Camera;
import gl.skybox.Skybox2D;
import io.Input;
import map.architecture.components.GhostPoi;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.hostile.GhostEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;

public class MapScene extends PlayableScene {
	
	private Skybox2D skybox;
	private ItemHandler itemHandler;
	private GhostEntity ghost;
	
	public MapScene() {
		super();
		Application.scene = this;		// Hack to make the variable update before constructors runs
		camera.setPosition(new Vector3f(0,10,0));
		
		player = new PlayerEntity(camera);
		arcHandler.load(this, new Vector3f(), PlayableScene.currentMap);
		EntityHandler.addEntity(player);
		arcHandler.getArchitecture().callCommand("spawn_player");
		
		ghost = new GhostEntity(player, arcHandler.getArchitecture().getNavigation());
		EntityHandler.addEntity(ghost);
		GhostPoi poi = ghost.findNextPoi();
		ghost.pos.set(poi.getPosition());
		ghost.pos.y += ghost.getBBox().getHeight();
		ghost.changeTarget();
		
		//AudioHandler.loop("white_noise");
		AudioHandler.loop("cicadas");
		
		if (arcHandler.isSkyboxEnabled())
			skybox = new Skybox2D();
		
		itemHandler = new ItemHandler(this, viewModelHandler);
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
		
		itemHandler.update();
		
		if (ui.isPaused()) return;
		if (isLoading) {
			isLoading = false;
		}
		
		//speechRecog.update();
		
		if (PlayerEntity.getHp() <= 0 && Camera.offsetY < PlayerHandler.CAMERA_STANDING_HEIGHT - .2f) {
			int key = Input.getAny();
			
			if (key == 0 || Console.isVisible()) {
				return;
			}
			
			player.reset();
			Console.send("map "+PlayableScene.currentMap);
		}
		
	}

	@Override
	public void render(Vector4f clipPlane) {
		super.render(clipPlane);
		if (arcHandler.isSkyboxEnabled())
			skybox.render(camera, Vector3f.Y_AXIS);
	}
	
	@Override
	public void postRender() {
		viewModelHandler.render(this);
	}

	public GhostEntity getGhost() {
		return ghost;
	}

}
