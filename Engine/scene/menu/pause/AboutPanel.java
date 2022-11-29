package scene.menu.pause;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import ui.UI;
import ui.menu.GuiButton;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.layout.GuiRowLayout;
import ui.menu.listener.MenuListener;
import util.Colors;

public class AboutPanel extends GuiPanel {
	
	private GuiButton back;

	public AboutPanel(GuiPanel parent) {
		super(parent, 264, 300, 762, 520);
		GuiRowLayout rowLayout = new GuiRowLayout(192, 24);
		setLayout(rowLayout, x, y, UI.width, UI.height);
		
		add(new GuiLabel(x, y, "Creative Design:   "));
		add(new GuiLabel(x, y, "#bMoose Low"));
		rowLayout.nextRow();
		add(new GuiLabel(x, y, "Programming/Art:"));
		add(new GuiLabel(x,y, " #pJason Heller"));
		hyperlink("#c<Github>", "https://github.com/jheller9");
		hyperlink("#c<Youtube>", "https://www.youtube.com/channel/UCebkCfxOlJHw4DolaLcSdmw");
		rowLayout.nextRow();
		add(new GuiLabel(x, y, "Music/Art:"));
		add(new GuiLabel(x, y, "#*DJ Bamberino & Ces"));
		hyperlink("#c<Bandcamp>", "https://dj-bamberino.bandcamp.com");
		hyperlink("#c<Youtube>", "https://www.youtube.com/user/minmax5");
		
		rowLayout.nextRow();
		this.back = new GuiButton(x, y, "Back");
		back.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				setFocus(false);
			}

		});
		add(back);
	}
	
	private void hyperlink(String title, String url) {
		GuiButton btn = new GuiButton(x, y, title);
		btn.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}

		});
		add(btn);
	}

	@Override
	public void update() {
		super.update();
		if (this.hasFocus) {
			UI.drawRect(0, 0, UI.width, UI.height, Colors.BLACK).setOpacity(.5f);
		}
	}

	@Override
	public void close() {
		super.close();
	}
}
