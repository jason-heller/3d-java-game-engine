package ui.menu;

import io.Input;
import ui.Font;
import ui.Image;
import ui.Text;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiSpinner extends GuiElement {
	private int index;
	private float textWidth = 128;
	private MenuListener listener = null;

	private final Text text;
	private final Text[] options;

	private final Image lArrow, rArrow;

	public GuiSpinner(float x, float y, String label, int index, String... options) {
		this.x = x;
		this.y = y;
		this.text = new Text(Font.defaultFont, label, 0, 0, Font.defaultSize, false);
		this.options = new Text[options.length];
		this.index = index;
		
		float longestStrLength = 0;
		for(int i = 0; i < options.length; i++) {
			this.options[i] = new Text(Font.defaultFont, options[i], 0, -4, Font.defaultSize, true);
			longestStrLength = Math.max(longestStrLength, this.options[i].getWidth());
		}

		width = (int) longestStrLength + 32;
		textWidth = (int) (32 + text.getWidth());

		height = (int) (text.getHeight() + 16);

		lArrow = new Image("gui_arrow", x + 16 + textWidth, y + 9);
		rArrow = new Image("gui_arrow", x + 16 + width + textWidth, y + 9);
		lArrow.setUvOffset(0, 0, -1, 1);
		lArrow.setDepth(9);
		rArrow.setDepth(9);
		rArrow.setCentered(true);
		lArrow.setCentered(true);
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	@Override
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		lArrow.x = x + 16 + textWidth;
		rArrow.x = x + 16 + width + textWidth;
		lArrow.y = y + 8;
		rArrow.y = y + 8;
	}

	@Override
	public void update() {
		text.setPosition(x, y - 6);
		UI.drawString(text);

		if (!tempDisable && hasFocus && Input.getMouseX() > x + textWidth
				&& Input.getMouseX() < x + width + textWidth + 32 && Input.getMouseY() > y
				&& Input.getMouseY() < y + height) {
			if (Input.isPressed("use_left")) {
				if (Input.getMouseX() < x + textWidth + width / 2f) {
					index--;
					if (index < 0) {
						index = options.length - 1;
					}
				} else {
					index++;
					if (index == options.length) {
						index = 0;
					}
				}
				hasFocus = true;

				if (listener != null) {
					listener.onClick(options[index].getText(), index);
				}
			}
		}

		UI.drawImage(lArrow);
		UI.drawImage(rArrow);
		// gui.drawImage("gui_arrow", x,y);
		// gui.drawImage("gui_arrow", x+16+128,y);
		options[index].setPosition(x + 16 + (int) textWidth + width / 2, y);
		UI.drawString(options[index]);
	}
}
