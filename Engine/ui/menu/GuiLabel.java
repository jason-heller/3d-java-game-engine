package ui.menu;

import ui.Font;
import ui.Text;
import ui.UI;

public class GuiLabel extends GuiElement {

	private final Text text;

	public GuiLabel(float x, float y, String option) {
		this.x = x;
		this.y = y;
		this.text = new Text(Font.defaultFont, option, 0, 0, Font.defaultSize, false);

		int lineHeight = Font.defaultFont.getPaddingHeight() + 20;
		height = lineHeight;
		width = (int) (text.getWidth());
	}

	public void center() {
		x = x - width / 2;
	}

	@Override
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		/*text.setPosition(x, y);
		text.setText(text.getText());
		UI.drawString(text);*/
		UI.drawString(text.getText(), x, y);	// Normally we can just draw the text, but labels can potentially have
												// animated text, so we draw it this way (jank)
	}
}
