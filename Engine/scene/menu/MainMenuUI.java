package scene.menu;

import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import audio.SoundEffects;
import audio.SoundFilters;
import audio.Source;
import core.Application;
import core.Resources;
import dev.Console;
import gl.res.Texture;
import io.Input;
import io.Settings;
import scene.PlayableScene;
import scene.Scene;
import scene.menu.pause.AboutPanel;
import scene.menu.pause.OptionsPanel;
import scene.singlearc.SingleArcScene;
import ui.Image;
import ui.Text;
import ui.UI;
import ui.menu.GuiMenu;
import ui.menu.listener.MenuListener;

public class MainMenuUI {

	private final Text title;
	private final Image background;
	private final GuiMenu mainMenu;
	private final OptionsPanel options;
	private final AboutPanel about;

	private final Texture mainMenuBg;
	
	private final Scene scene;
	private final Source musicSource;
	
	public static boolean onIntroSplash = true;
	public static boolean disableIntroSplash = false;
	
	public MainMenuUI(Scene scene) {
		this.scene = scene;
		Resources.addSound("mus01_intro", "music/sheebyweeby.ogg");
		musicSource = AudioHandler.playMusic("mus01_intro");
		
		mainMenuBg = Resources.addTexture("main_menu_bg", "gui/menu.png");

		mainMenu = new GuiMenu(50, 300, "new game", "continue", "options", "about", "quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);
		about = new AboutPanel(null);

		title = new Text(Application.TITLE, 50, 125, .75f, false);
		
		background = new Image(mainMenuBg, 0, 0, (int) UI.width, (int) UI.height);

		
		
		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (onIntroSplash)
					return;
				
				switch (index) {
				case 0:
					about.setFocus(false);
					if (options.isFocused()) {
						Settings.save();
						options.setFocus(false);
					}
					PlayableScene.currentMap = "map01";
					Application.changeScene(SingleArcScene.class);
					musicSource.stop();
					break;
				case 1:
					about.setFocus(false);
					if (options.isFocused()) {
						Settings.save();
						options.setFocus(false);
					}
					break;
				case 2:
					options.setFocus(!options.isFocused());
					about.setFocus(false);
					break;
				case 3:
					options.setFocus(false);
					about.setFocus(true);
					break;
				case 4:
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
		mainMenuBg.delete();
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
		} else {
			mainMenu.draw();
		}

		scene.getCamera().updateViewMatrix();
	
	}

	private void closeSplashScreen() {
		onIntroSplash = false;
		UI.addComponent(background);
		UI.addComponent(title);
		musicSource.applyEffect(SoundEffects.REVERB);
		musicSource.applyFilter(SoundFilters.LOW_PASS_FILTER);
	}

	private void drawIntroSplash() {
		UI.drawString(Application.TITLE, 640, 125, 1f, true);
		UI.drawString("#wPress [space]", 640, 640, true);
		scene.getCamera().updateViewMatrix();
		
	}
}
