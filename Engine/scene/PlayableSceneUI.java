package scene;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
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
	
	private final int CROSSHAIR_SIZE = 8;
	private final int CROSSHAIR_THICKNESS = 1;
	private final Vector3f CROSSHAIR_COLOR = new Vector3f(1, 1, 1);
	
	private boolean paused;
	
	public PlayableSceneUI(PlayableScene scene) {
		this.scene = scene;
		mainMenu = new GuiMenu(50, 300, "resume", "options", "quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);
		
		Resources.addTexture("hp", "gui/hp.png");

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
					Application.changeScene(MainMenu.class);
					break;
				}
			}

		});
	}
	
	public void cleanUp() {
		Resources.getTexture("hp").delete();
	}
	
	public void update() {
		if (scene.isLoading) {
			UI.drawRect(0, 0, 1280, 720, Colors.BLACK).setDepth(-999);
			UI.drawString("Loading", 640, 360, true).setDepth(-1000);
			return;
		}
		
		UI.drawString(Application.VERSION, 5, 5, .25f, false);

		UI.drawRect(639 - CROSSHAIR_THICKNESS, 359 - CROSSHAIR_SIZE, 2 * CROSSHAIR_THICKNESS + 2, 2 * CROSSHAIR_THICKNESS + 2,
				Colors.BLACK);
		UI.drawRect(640 - CROSSHAIR_THICKNESS, 360 - CROSSHAIR_SIZE, 2 * CROSSHAIR_THICKNESS, 2 * CROSSHAIR_THICKNESS,
				CROSSHAIR_COLOR);
		if (Input.isPressed(Controls.get("pause"))) {
			if (!paused) {
				PlayerHandler.disable();
				Input.requestMouseRelease();
				paused = true;
				AudioHandler.pause();
			} else {
				unpause();
			}
		}
		
		if (paused) {
			UI.drawRect(0, 0, 1280, 720, Colors.BLACK).setOpacity(.5f);
			if (options.isFocused()) {
				options.update();
				options.draw();
			} else {
				mainMenu.draw();
			}
		}
	
	}

	private void unpause() {
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
