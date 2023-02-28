package scene;

import scene.menu.MainMenuUI;
import ui.Font;
import ui.UI;
import ui.menu.GuiMenu;
import ui.menu.GuiPanel;
import ui.menu.layout.GuiRowLayout;
import ui.menu.listener.MenuListener;
import util.Colors;

public class MapPanel extends GuiPanel {
	
	private final GuiMenu menu;
	
	public MapPanel(GuiPanel parent, MainMenuUI ui) {
		super(parent, 264, 300, 762, 520);
		GuiRowLayout rowLayout = new GuiRowLayout(192, 24);
		setLayout(rowLayout, x, y, UI.width, UI.height);
	
		menu = new GuiMenu(300, 300, "Railyard", "Blue Hill Plaza", "Test Room", "Back");
		menu.setFont(Font.vhsFont);
		add(menu);
		
		menu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				switch (index) {
				case 0:
					ui.changeMap("map01");
					break;
				case 1:
					ui.changeMap("map02");
					break;
				case 2:
					ui.changeMap("light_test");
					break;
				case 3:
					setFocus(false);
					break;
				}
			}

		});
	}

	@Override
	public void update() {
		super.update();
		if (this.hasFocus) {
			UI.drawRect(0, 0, UI.width, UI.height, Colors.BLACK).setOpacity(.5f);
			menu.draw();
		}
	}

	@Override
	public void close() {
		super.close();
	}
}
