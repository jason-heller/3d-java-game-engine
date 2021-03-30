package scene;

import gl.Camera;
import scene.menu.MainMenuUI;

public class MainMenu implements Scene {
	
	private MainMenuUI ui;
	private Camera camera = new Camera();
	
	public MainMenu() {
		ui = new MainMenuUI(this);
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
	public void render() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update() {
		ui.update();
	}
}
