package scene;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.App;
import core.Resources;
import io.Controls;
import io.Input;
import io.Settings;
import scene.entity.util.PlayerHandler;
import scene.menu.pause.OptionsPanel;
import ui.UI;
import ui.menu.GuiMenu;
import ui.menu.listener.MenuListener;
import util.Colors;

public class PlayableSceneUI {

	private final GuiMenu mainMenu;
	private final OptionsPanel options;
	
	private final PlayableScene scene;
	
	private boolean paused;
	
	public PlayableSceneUI(PlayableScene scene) {
		this.scene = scene;
		mainMenu = new GuiMenu(50, 300, "resume", "options", "quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);

		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				
				switch (index) {
				case 0:
					unpause();
					break;
				case 1:
					options.setFocus(!options.isFocused());
					break;
				case 2:
					options.setFocus(false);
					App.changeScene(MainMenu.class);
					break;
				}
			}

		});
	}
	
	public void cleanUp() {
	}
	
	public void update() {
		if (scene.isLoading)
			return;
		
		UI.drawString(App.VERSION, 5, 5, .15f, false);

		if (Input.isPressed(Controls.get("pause"))) {
			if (!paused) {
				pause();
			} else {
				unpause();
			}
		}
		
		if (paused) {
			UI.drawRect(0, 0, UI.width, UI.height, Colors.BLACK).setOpacity(.5f);
			if (options.isFocused()) {
				options.update();
				options.draw();
			} else {
				mainMenu.draw();
			}
		}
	
	}

	public void pause() {
		PlayerHandler.disable();
		Input.requestMouseRelease();
		paused = true;
		AudioHandler.pause();
	}

	public void unpause() {
		if (options.isFocused()) {
			options.setFocus(false);
			Settings.grabData();
			Settings.save();
		} else {
			paused = false;
			AudioHandler.unpause();
			PlayerHandler.enable();
			//if (!Console.isVisible()) {
				Input.requestMouseGrab();
			//}
		}
	}

	public boolean isPaused() {
		return paused;
	}
}
