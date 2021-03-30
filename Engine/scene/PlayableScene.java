package scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Resources;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.Window;
import map.architecture.Architecture;
import map.architecture.ArchitectureHandler;
import scene.entity.EntityHandler;
import scene.entity.utility.PlayerEntity;
import scene.entity.utility.PlayerHandler;
import ui.UI;

public abstract class PlayableScene implements Scene {

	protected Camera camera;
	
	protected PlayableSceneUI ui;
	protected EntityHandler entityHandler;

	protected ArchitectureHandler arcHandler;

	protected PlayerEntity player;
	
	private Vector3f cameraLight;
	
	private TexturedModel walker;
	
	protected boolean isLoading = true;
	
	public static String currentMap;
	
	public PlayableScene() {
		camera = new Camera();
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.grabMouse();
		cameraLight = new Vector3f(1,1,1);
		
		ui = new PlayableSceneUI(this);
		
		arcHandler = new ArchitectureHandler();
		
		entityHandler = new EntityHandler();
		
		AssetPool.loadInGameAssets();
		Resources.addObjModel("walker", "item/walker.obj");
		walker = new TexturedModel("walker", "item1", new Matrix4f());
		
		
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
			Mouse.setCursorPosition(Window.getWidth()/2, Window.getHeight()/2);
		}
		
		entityHandler.update(this);
		camera.move();
	}
	
	@Override
	public void render() {

		Architecture arc = arcHandler.getArchitecture();
		Vector3f targetLight = arc.getLightAtPosition(camera.getPosition(), camera.getDirectionVector());
		cameraLight.set(Vector3f.lerp(targetLight, cameraLight, 10f * Window.deltaTime));

		arcHandler.render(camera);
		entityHandler.render(camera, arc);
		
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
