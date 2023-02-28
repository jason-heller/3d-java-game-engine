package scene.menu;

import org.lwjgl.input.Keyboard;

import core.App;
import dev.cmd.Console;
import io.Input;
import io.Settings;
import map.architecture.ArchitectureHandler;
import scene.MapPanel;
import scene.PlayableScene;
import scene.Scene;
import scene.mapscene.MapScene;
import scene.menu.pause.AboutPanel;
import scene.menu.pause.OptionsPanel;
import ui.Font;
import ui.Text;
import ui.UI;
import ui.menu.GuiMenu;
import ui.menu.listener.MenuListener;

public class MainMenuUI {

	private final Text title;
	//private final Image background;
	private final GuiMenu mainMenu;
	private final OptionsPanel options;
	private final AboutPanel about;
	private final MapPanel maps;

	//private final Texture mainMenuBg;
	
	private final Scene scene;
	
	public static boolean onIntroSplash = true;
	public static boolean disableIntroSplash = false;
	
	public MainMenuUI(Scene scene) {
		this.scene = scene;
		
		//mainMenuBg = Resources.addTexture("main_menu_bg", "gui/menu.png");

		mainMenu = new GuiMenu(50, 300, "play game", "options", "about", "quit");
		mainMenu.setFocus(true);
		mainMenu.setFont(Font.vhsFont);
		
		options = new OptionsPanel(null);
		about = new AboutPanel(null);
		maps = new MapPanel(null, this);

		title = new Text(Font.vhsFont, App.TITLE, 50, 125, .75f, Integer.MAX_VALUE, false);
		
		//background = new Image(mainMenuBg, 0, 0, (int) UI.width, (int) UI.height);
		
		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (onIntroSplash)
					return;
				
				switch (index) {
				case 0:
					maps.setFocus(true);
					about.setFocus(false);
					
					if (options.isFocused()) {
						Settings.save();
						options.setFocus(false);
					}
					break;
				/*case 1:
					about.setFocus(false);
					maps.setFocus(false);
					if (options.isFocused()) {
						Settings.save();
						options.setFocus(false);
					}
					break;*/
				case 1:
					options.setFocus(!options.isFocused());
					about.setFocus(false);
					maps.setFocus(false);
					break;
				case 2:
					options.setFocus(false);
					about.setFocus(true);
					maps.setFocus(false);
					break;
				case 3:
					Console.send("quit");
					break;
				}
			}

		});
		
		if (!onIntroSplash) {
			closeSplashScreen();
		}
	}
	
	public void cleanUp() {
		//mainMenuBg.cleanUp();
	}
	
	public void update() {
		if (onIntroSplash) {
			drawIntroSplash();
			if (Input.isPressed(Keyboard.KEY_SPACE)) {
				closeSplashScreen();
				
			}
			return;
		}
		
		if (Input.isPressed("pause")) {
			options.setFocus(false);
			about.setFocus(false);
		}
		
		if (options.isFocused()) {
			options.update();
			options.draw();
		} else if (about.isFocused()) {
			about.update();
			about.draw();
		} else if (maps.isFocused()) {
			maps.update();
			maps.draw();
		} else {
			mainMenu.draw();
		}

		scene.getCamera().updateViewMatrix();
	
	}

	private void closeSplashScreen() {
		onIntroSplash = false;
		//UI.addComponent(background, bgMatrix);
		UI.addComponent(title);
	}

	private void drawIntroSplash() {
		UI.drawString(App.TITLE, 640, 125, 1f, true);
		UI.drawString("#wPress [space]", 640, 640, true);
		scene.getCamera().updateViewMatrix();
	}
	
	public void changeMap(String map) {
		if (!ArchitectureHandler.isValidMap(map)) {
			Console.warning("No such map: " + map);
			return;
		}
		
		PlayableScene.currentMap = map;
		scene.cleanUp();
		App.changeScene(MapScene.class);
	}
}
