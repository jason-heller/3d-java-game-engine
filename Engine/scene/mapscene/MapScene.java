package scene.mapscene;

import org.joml.Vector3f;
import org.joml.Vector4f;

import audio.speech.SpeechHandler;
import core.Application;
import dev.cmd.Console;
import gl.Camera;
import gl.Window;
import gl.skybox.Skybox;
import gl.skybox.Skybox2D;
import gl.skybox._3d.Skybox3D;
import gl.skybox._3d.SkyboxCamera;
import io.Input;
import map.architecture.components.GhostPoi;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.hostile.GhostEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;
import ui.UI;

public class MapScene extends PlayableScene {
	
	private static final float HOUR_LENGTH = 120;
	private Skybox skybox;
	private ItemHandler itemHandler;
	private GhostEntity ghost;
	
	private float time = 0f;
	
	public MapScene() {
		super();
		Application.scene = this;		// Hack to make the variable update before constructors runs
		
		player = new PlayerEntity(camera);
		arcHandler.load(this, new Vector3f(), PlayableScene.currentMap);
		EntityHandler.addEntity(player);
		arcHandler.getArchitecture().callCommand("spawn_player");
		arcHandler.getArchitecture().callCommand(player, "trigger_soundscape");
		if (arcHandler.getArchitecture().bsp.rooms.length > 1) {
			ghost = new GhostEntity(player, arcHandler.getArchitecture().getNavigation());
			EntityHandler.addEntity(ghost);
			GhostPoi poi = ghost.findNextPoi();
			ghost.pos.set(poi.getPosition());
			ghost.pos.y += ghost.getBBox().getHeight();
			ghost.changeTarget();
		} else {
			Console.warning("No rooms exist in this map, cannot spawn ghost.");
		}
		
		//AudioHandler.loop("white_noise");
		//AudioHandler.loop("cicadas");
		
		if (arcHandler.isSkyboxEnabled()) {
			SkyboxCamera skyCam = arcHandler.getArchitecture().getSkyCamera();
			if (skyCam != null) {
				skybox = new Skybox3D(skyCam, arcHandler.getArchitecture().bsp, arcHandler.getArchitecture().pvs);
			} else {
				skybox = new Skybox2D();
			}
		}
		
		itemHandler = new ItemHandler(this, viewModelHandler);
		
		SpeechHandler.start();
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		SpeechHandler.stop();
		if (arcHandler.isSkyboxEnabled())
			skybox.cleanUp();
	}

	@Override
	public Camera getCamera() {
		return camera;
	}
	
	@Override
	public void update() {
		if (isLoading) {
			isLoading = false;
		}
		super.update();
		
		time += Window.deltaTime;
		int hours = (int)(time / HOUR_LENGTH);
		String time = "TIME: 0" + hours + ":00" + " AM";
		UI.drawString(time, 640, 10, .3f, true);
		
		itemHandler.update();
		
		if (ui.isPaused()) return;
		
		//speechRecog.update();
		
		if (PlayerEntity.getHp() <= 0 && Camera.offsetY < PlayerHandler.CAMERA_STANDING_HEIGHT - .2f) {
			int key = Input.getAny();
			
			if (key == 0 || Console.isVisible()) {
				return;
			}

			player.reset();
			Console.send("map " + PlayableScene.currentMap);
		}

	}

	@Override
	public void render(Vector4f clipPlane) {
		if (arcHandler.isSkyboxEnabled())
			skybox.render(arcHandler.getArchitecture(), camera);
		
		super.render(clipPlane);
		
	}
	
	@Override
	public void postRender() {
		viewModelHandler.render(this);
	}

	public GhostEntity getGhost() {
		return ghost;
	}

}
