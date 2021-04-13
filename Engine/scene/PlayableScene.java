package scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.Window;
import map.architecture.Architecture;
import map.architecture.ArchitectureHandler;
import scene.entity.EntityHandler;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;
import ui.UI;

public abstract class PlayableScene implements Scene {

	protected Camera camera;
	
	protected PlayableSceneUI ui;
	protected EntityHandler entityHandler;

	protected ArchitectureHandler arcHandler;

	protected PlayerEntity player;
	
	private Vector3f[] cameraLight = new Vector3f[6];
	
	private TexturedModel walker;
	
	protected boolean isLoading = true;
	
	public static String currentMap;
	
	public PlayableScene() {
		camera = new Camera();
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.grabMouse();
		
		for(int i = 0; i < 6; i++) {
			cameraLight[i] = new Vector3f(1,1,1);
		}
		
		AssetPool.loadInGameAssets();
		Resources.addObjModel("walker", "item/walker.obj");
		walker = new TexturedModel("walker", "item1", new Matrix4f());
		
		ui = new PlayableSceneUI(this);
		
		arcHandler = new ArchitectureHandler();
		
		entityHandler = new EntityHandler();
		
		ui.update();
		UI.render(this);
		Window.update();
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
	public void render(float clipX, float clipY, float clipZ, float clipDist) {

		Architecture arc = arcHandler.getArchitecture();
		Vector3f[] targetLight = arc.getLightsAt(camera.getPosition());
		for(int i = 0; i < 6; i++) {
			cameraLight[i].set(Vector3f.lerp(targetLight[i], cameraLight[i], 10f * Window.deltaTime));
		}

		arcHandler.render(camera, clipX, clipY, clipZ, clipDist);
		entityHandler.render(camera, arc);
	}
	
	@Override
	public void renderNoReflect() {
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
		
		if (PlayerHandler.hasWalker) {
			Matrix4f m = camera.getViewModelMatrix(true);
			walker.getMatrix().set(m);
			Render.renderViewModel(walker, cameraLight);
		}
	}
	
	@Override
	public void cleanUp() {
		AssetPool.unload();
		walker.getModel().cleanUp(); 
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
}
