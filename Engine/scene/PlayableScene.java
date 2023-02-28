package scene;

import org.joml.Vector4f;
import org.lwjgl.input.Mouse;

import dev.Debug;
import gl.Camera;
import gl.Window;
import gl.anim.render.AnimationHandler;
import map.architecture.Architecture;
import map.architecture.ArchitectureHandler;
import scene.entity.EntityHandler;
import scene.entity.util.PlayerEntity;
import scene.mapscene.AssetPool;
import scene.viewmodel.ViewModelHandler;
import ui.UI;

public abstract class PlayableScene implements Scene {

	protected Camera camera;
	
	protected PlayableSceneUI ui;
	protected EntityHandler entityHandler;

	protected ArchitectureHandler arcHandler;

	protected PlayerEntity player;
	
	protected boolean isLoading = true;
	
	public static String currentMap;
	
	protected ViewModelHandler viewModelHandler;
	
	public PlayableScene() {
		camera = new Camera();
		camera.grabMouse();
		
		AssetPool.loadInGameAssets();
		
		ui = new PlayableSceneUI(this);
		
		arcHandler = new ArchitectureHandler();
		
		AnimationHandler.init();
		entityHandler = new EntityHandler();
		
		ui.update();
		UI.render(this);
		Window.update();
		
		viewModelHandler = new ViewModelHandler();
		PlayerEntity.enabled = true;
	}

	@Override
	public void tick() {
		if (ui.isPaused()) return;
	}
	
	@Override
	public void update() {
		if (isLoading) {
			Window.resetDeltaTime();		// HACKY
		}
		
		ui.update();
		if (ui.isPaused()) return;
		
		if (Mouse.isGrabbed()) {
			Mouse.setCursorPosition(Window.getWidth() / 2, Window.getHeight() / 2);
		}
		
		if (!Window.isActive()) {
			ui.pause();
		}
		
		arcHandler.update(camera);
		
		entityHandler.update(this);
		camera.move();
		
		if (Debug.debugMode) {
			Debug.update(this);
		}
	}
	
	@Override
	public void render(Vector4f clipPlane) {

		Architecture arc = arcHandler.getArchitecture();

		arcHandler.debugRender(camera);
		arcHandler.render(camera, clipPlane, true);
		entityHandler.render(camera, arc, clipPlane);
		AnimationHandler.render(camera, this);
	}
	
	@Override
	public void fastRender(Vector4f clipPlane) {

		Architecture arc = arcHandler.getArchitecture();
		arcHandler.render(camera, clipPlane, false);
		entityHandler.render(camera, arc, clipPlane);
		AnimationHandler.render(camera, this);
	}
	
	@Override
	public void renderNoReflect() {
	}
	
	@Override
	public void cleanUp() {
		AssetPool.unload();
		arcHandler.cleanUp();
		entityHandler.cleanUp();
		AnimationHandler.cleanUp();
	}

	public PlayableSceneUI getUi() {
		return ui;
	}
	
	public PlayerEntity getPlayer() {
		return player;
	}

	public ArchitectureHandler getArcHandler() {
		return arcHandler;
	}

	public Architecture getArchitecture() {
		return arcHandler.getArchitecture();
	}

	public ViewModelHandler getViewModelHandler() {
		return viewModelHandler;
	}
}
