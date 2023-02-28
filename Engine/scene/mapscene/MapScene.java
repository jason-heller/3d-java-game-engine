package scene.mapscene;

import org.joml.Vector3f;
import org.joml.Vector4f;

import audio.AudioHandler;
import core.App;
import dev.cmd.Console;
import gl.Camera;
import gl.Window;
import gl.skybox.Skybox;
import gl.skybox.Skybox2D;
import gl.skybox._3d.Skybox3D;
import gl.skybox._3d.SkyboxCamera;
import io.Input;
import io.MusicHandler;
import scene.MainMenu;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PreSessionEntity;
import scene.entity.util.SkatePhysicsEntity;
import scene.mapscene.trick.TrickList;
import ui.UI;
import util.Colors;

public class MapScene extends PlayableScene {
	
	public static boolean preRound;
	private Skybox skybox;
	private ItemHandler itemHandler;
	
	private int score;
	private float time = 121;
	
	private MusicHandler musicHandler;
	
	public MapScene() {
		super();
		App.scene = this;		// Hack to make the variable update before constructors runs
		preRound = true;
		
		TrickList.init();
		
		player = new PlayerEntity(this);
		camera.setControlStyle(Camera.THIRD_PERSON, player);
		
		boolean mapLoadSucceeded = arcHandler.load(this, new Vector3f(), PlayableScene.currentMap);
		
		if (!mapLoadSucceeded) {
			App.changeScene(MainMenu.class);
			return;
		}
		
		EntityHandler.addEntity(player);
		arcHandler.getArchitecture().callCommand("spawn_player");
		// arcHandler.getArchitecture().callCommand(player, "trigger_soundscape");
		player.getPosition().y += 5;
		SkatePhysicsEntity.direction = -player.rotation.y;
		player.update(this);
		
		//EntityHandler.addEntity(new MapOverviewEntity(this, player, arcHandler.getArchitecture().bsp));
		EntityHandler.addEntity(new PreSessionEntity(player));
		
		if (arcHandler.isSkyboxEnabled()) {
			SkyboxCamera skyCam = arcHandler.getArchitecture().getSkyCamera();
			if (skyCam != null) {
				skybox = new Skybox3D(skyCam, arcHandler.getArchitecture().bsp, arcHandler.getArchitecture().pvs);
			} else {
				skybox = new Skybox2D();
			}
		}
		
		itemHandler = new ItemHandler(this, viewModelHandler);
		
		musicHandler = new MusicHandler();
		musicHandler.loadQueue(2);
		
		musicHandler.playNext();
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
		
		musicHandler.stopMusic();
		musicHandler.cleanUp();
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

		musicHandler.update();
		super.update();
		
		itemHandler.update();
		
		if (ui.isPaused())
			return;
		
		if (PlayerEntity.getHp() <= 0) {
			int key = Input.getAny();
			
			if (key == 0 || Console.isVisible()) {
				return;
			}

			player.reset();
			Console.send("map " + PlayableScene.currentMap);
		}

		if (!preRound) {
			time -= Window.deltaTime;
			UI.drawRect(0, 540, 200, 120, Colors.BLACK).setOpacity(.75f);
			UI.drawString("" + score, 100, 590, .35f, false);
			UI.drawString(formatTime(time), 100, 630, .35f, false);
			
		}
		
		if (player.isSwitch()) {
			UI.drawString("(Switch)", 100, 510);
		}
	}

	private String formatTime(float value) {
		int t = (int)value;
		int min = t / 60;
		int sec = t % 60;
		
		return String.format("%02d", min) + ":" + String.format("%02d", sec);
	}

	@Override
	public void render(Vector4f clipPlane) {
		if (arcHandler.isSkyboxEnabled())
			skybox.render(arcHandler.getArchitecture(), camera);
		
		super.render(clipPlane);
		
	}
	
	@Override
	public void fastRender(Vector4f clipPlane) {
		if (arcHandler.isSkyboxEnabled())
			skybox.render(arcHandler.getArchitecture(), camera);
		
		super.fastRender(clipPlane);
		
	}
	
	@Override
	public void postRender() {
		viewModelHandler.render(this);
	}

	public void addScore(int score) {
		this.score += score;
	}

	public int getScore() {
		return score;
	}
	
	public MusicHandler getMusicHandler() {
		return musicHandler;
	}

}
