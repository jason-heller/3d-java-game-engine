package scene.menu.pause;

import dev.Debug;
import scene.menu.MainMenuUI;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.GuiSpinner;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;

public class AdvancedPanel extends GuiPanel {
	
	private final GuiSpinner disableIntro, allowConsole;
	
	public AdvancedPanel(GuiPanel parent, float x, float y, float width, float height) {
		super(parent, x, y, width, height);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);

		add(new GuiLabel(x, y ,"#SMisc."));
		
		disableIntro = new GuiSpinner(x, y, "Disable Splash Screen", MainMenuUI.disableIntroSplash ? 1 : 0, "True", "False");
		disableIntro.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (index == 1) {
					MainMenuUI.disableIntroSplash = true;
				} else {
					MainMenuUI.disableIntroSplash = false;
				}
			}
		});
		add(disableIntro);
		
		allowConsole = new GuiSpinner(x, y, "Enable Console", Debug.allowConsole ? 0 : 1, "True", "False");
		allowConsole.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				Debug.allowConsole = !Debug.allowConsole;
			}
		});
		add(allowConsole);
		
	}
}
