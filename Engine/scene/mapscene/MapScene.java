package scene.mapscene;

import org.joml.Vector3f;
import org.joml.Vector4f;

import core.App;
import dev.cmd.Console;
import gl.Camera;
import gl.skybox.Skybox;
import gl.skybox.Skybox2D;
import gl.skybox._3d.Skybox3D;
import gl.skybox._3d.SkyboxCamera;
import io.Input;
import scene.MainMenu;
import scene.PlayableScene;
import scene.entity.DummyEntity;
import scene.entity.EntityHandler;
import scene.entity.util.PlayerEntity;
import scene.mapscene.trick.TrickList;
import ui.UI;

public class MapScene extends PlayableScene {
	
	private Skybox skybox;
	private ItemHandler itemHandler;
	
	private int score;
	
	public MapScene() {
		super();
		App.scene = this;		// Hack to make the variable update before constructors runs
		
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
		
		if (arcHandler.isSkyboxEnabled()) {
			SkyboxCamera skyCam = arcHandler.getArchitecture().getSkyCamera();
			if (skyCam != null) {
				skybox = new Skybox3D(skyCam, arcHandler.getArchitecture().bsp, arcHandler.getArchitecture().pvs);
			} else {
				skybox = new Skybox2D();
			}
		}
		
		itemHandler = new ItemHandler(this, viewModelHandler);
		
		TrickList.init();
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
		if (isLoading) {
			isLoading = false;
		}

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

		UI.drawString("" + score, 50, 680);
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

}
