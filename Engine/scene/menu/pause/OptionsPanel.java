package scene.menu.pause;

import io.Settings;
import ui.menu.GuiLayeredPane;
import ui.menu.GuiPanel;

public class OptionsPanel extends GuiLayeredPane {
	private final GraphicsPanel gfx;
	private final ControlsPanel controls;
	private final SoundPanel sfx;
	private final GameplayPanel gameplay;
	private final AdvancedPanel adv;

	public OptionsPanel(GuiPanel parent) {
		super(parent, 264, 100, 762, 520, "Options");

		setMenu("Controls", "Graphics", "Sound", "Gameplay", "Advanced", "Back");

		gfx = new GraphicsPanel(this, tabX + 4, y + 4, 758, 520);
		controls = new ControlsPanel(this, tabX + 4, y + 4, 758, 520);
		sfx = new SoundPanel(this, tabX + 4, y + 4, 758, 520);
		gameplay = new GameplayPanel(this, tabX + 4, y + 4, 758, 520);
		adv = new AdvancedPanel(this, tabX + 4, y +4, 758, 520);

		setPanels(controls, gfx, sfx, gameplay, adv);
	}
	
	@Override
	public void update() {
		getPane().update();
	}

	@Override
	public void close() {
		super.close();
		Settings.grabData();
	}
}
