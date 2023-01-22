package ui.menu;

import io.Input;
import ui.Font;
import ui.Text;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiButton extends GuiElement {
	private final int lineHeight;
	private boolean selected = false;
	private MenuListener listener = null;

	private final Text option;

	public GuiButton(float x, float y, String option) {
		this.x = x;
		this.y = y;
		this.option = new Text(Font.defaultFont, option, 0, 0, Font.defaultSize, false);

		lineHeight = Font.defaultFont.getPaddingHeight() + 20;
		height = (int) this.option.getHeight();
		width = (int) this.option.getWidth();
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	public void center() {
		x = x - width / 2;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	@Override
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		selected = false;
		if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x + width && Input.getMouseY() > y
				&& Input.getMouseY() < y + lineHeight) {
			selected = true;
			if (Input.isPressed("use_left") && listener != null) {
				listener.onClick(option.getText(), 0);
			}
		}

		option.setPosition(x, y);
		if (selected) {
			UI.drawString("#s" + option.getText(), x, y, false);
		} else {
			UI.drawString(option.getText(), x, y, false);
		}
	}
}
