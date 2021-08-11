package scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.input.Mouse;

import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.Window;
import map.architecture.Architecture;
import map.architecture.ArchitectureHandler;
import scene.entity.EntityHandler;
import scene.entity.util.PlayerEntity;
import scene.mapscene.AssetPool;
import scene.viewmodel.ViewModelHandler;
import ui.Text;
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
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.grabMouse();
		
		AssetPool.loadInGameAssets();
		
		ui = new PlayableSceneUI(this);
		
		arcHandler = new ArchitectureHandler();
		
		entityHandler = new EntityHandler();
		
		ui.update();
		UI.render(this);
		Window.update();
		
		viewModelHandler = new ViewModelHandler();
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
	}
	
	@Override
	public void render(Vector4f clipPlane) {

		Architecture arc = arcHandler.getArchitecture();
		Vector3f[] targetLight = arc.getLightsAt(camera.getPosition());

		arcHandler.debugRender(camera);
		arcHandler.render(camera, clipPlane);
		entityHandler.render(camera, arc, clipPlane);
	}
	
	@Override
	public void renderNoReflect() {
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
		
		// TODO: Viewmodels
		
		/*if (PlayerHandler.hasWalker) {
			Matrix4f m = camera.getViewModelMatrix(true);
			walker.getMatrix().set(m);
			Render.renderViewModel(walker, cameraLight);
		}*/
	}
	
	@Override
	public void cleanUp() {
		AssetPool.unload();
		arcHandler.cleanUp();
		entityHandler.cleanUp();
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
}
