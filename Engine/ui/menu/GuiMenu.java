package ui.menu;

import audio.AudioHandler;
import io.Input;
import ui.Font;
import ui.Text;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiMenu extends GuiElement {
	private final int lineHeight;
	private int selectedOption = -1;
	private MenuListener listener = null;
	private boolean centered = false;
	
	private int alignment = GuiComponent.VERTICAL;

	private final Text[] texts;
	private Font font = Font.defaultFont;
	private float fontSize =  Font.defaultSize;

	public GuiMenu(float x, float y, String... options) {
		this.x = x;
		this.y = y;
		this.texts = new Text[options.length];
		float maxWid = 0;
		for(int i = 0; i < options.length; i++) {
			this.texts[i] = new Text(Font.defaultFont, options[i], x, y, Font.defaultSize, false);
			maxWid = Math.max(maxWid, this.texts[i].getWidth());
		}

		lineHeight = Font.defaultFont.getLineHeight(Font.defaultSize) + 20;
		height = lineHeight * options.length;
		width = (int)(maxWid) + 16;
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
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
		selectedOption = -1;
		int index = 0;
		int dx = 0;
		int dy = 0;
		int i = -1;
		for (final Text text : texts) {
			String option = text.getText();

			if (alignment == GuiComponent.VERTICAL) {
				dy = index * lineHeight;
			} else if (i >= 0) {
				dx += texts[i].getWidth() + 16;
			}
			
			i++;
			int relWid = (int) ((alignment == GuiComponent.VERTICAL) ? width : texts[i].getWidth());
			
			if (!tempDisable && hasFocus && Input.getMouseX() > x + dx && Input.getMouseX() < x + dx + relWid
					&& Input.getMouseY() > y + dy
					&& Input.getMouseY() < y + dy + lineHeight) {
				selectedOption = index;
				if (Input.isPressed("use_left") && listener != null) {
					listener.onClick(option, index);
					AudioHandler.play("click");
				}
			}
			
			if (index == selectedOption) {
				UI.drawString(font, "#s" + option, x + dx, y + dy, fontSize, Integer.MAX_VALUE, false);
			} else {
				UI.drawString(font, option, x + dx, y + dy, fontSize, Integer.MAX_VALUE, false);
			}
			index++;
		}
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public void setFontSize(float size) {
		this.fontSize = size;
	}
	
	public void setOption(int index, String option) {
		texts[index].setText(option);
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}
}
