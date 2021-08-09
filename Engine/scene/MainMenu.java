package scene;

import org.joml.Vector4f;

import gl.Camera;
import map.architecture.ArchitectureHandler;
import scene.menu.MainMenuUI;

public class MainMenu implements Scene {
	
	private MainMenuUI ui;
	private Camera camera = new Camera();
	
	public MainMenu() {
		ui = new MainMenuUI(this);
		ArchitectureHandler.pollValidMaps();		// Poll anytime the menu is opened
	}
	
	@Override
	public void tick() {
		
	}

	@Override
	public void cleanUp() {
		ui.cleanUp();
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	public void render(Vector4f clipPlane) {
	}
	
	@Override
	public void renderNoReflect() {
	}
	
	@Override
	public void update() {
		ui.update();
	}

	@Override
	public void postRender() {
		// TODO Auto-generated method stub
		
	}
}
