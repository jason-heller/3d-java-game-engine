package ui.menu;

import io.Input;
import ui.Font;
import ui.Text;
import ui.UI;
import ui.menu.listener.SliderListener;
import util.Colors;

public class GuiSlider extends GuiElement {
	private final int txtWidth;
	private int sliderPos;
	private final float minValue, maxValue;
	private float value;
	private final float increment;
	private float offset;
	private SliderListener listener = null;

	private boolean hasFocus = false;

	private final Text text;
	private String prefix = "";

	public GuiSlider(float x, float y, String label, float minValue, float maxValue, float value, float increment) {
		this.x = x;
		this.y = y;
		this.text = new Text(Font.defaultFont, label, 0, -6, Font.defaultSize, false);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.value = value;
		this.increment = increment;
		if (value % increment != 0) {
			offset = value % increment;
		}

		height = 16;
		width = 150;
		txtWidth = (int)(text.getWidth() + 64) / 64 * 64;
		sliderPos = (int) ((value - minValue) / (maxValue - minValue) * width);
	}

	public void addListener(SliderListener listener) {
		this.listener = listener;
	}

	public float getValue() {
		return value;
	}

	@Override
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void setTextPrefix(String prefix) {
		String origTxt = text.getText().replace(this.prefix, "");
		this.prefix = prefix;
		text.setText(prefix + origTxt);
	}

	public void setValue(int value) {
		this.value = value;
		sliderPos = (int) ((value - minValue) / (maxValue - minValue) * width);
	}

	@Override
	public void update() {
		text.setPosition(x, y);
		UI.drawString(text);

		if (hasFocus || Input.getMouseX() > x + txtWidth && Input.getMouseX() < x + width + txtWidth
				&& Input.getMouseY() > y && Input.getMouseY() < y + height) {
			
			if (Input.isPressed("use_left")) {
				hasFocus = true;
			}
			
			if (Input.isDown("use_left") && hasFocus) {
				value = minValue + (Input.getMouseX() - ((float) x + txtWidth)) / width * (maxValue - minValue);
				value = offset + (float) (Math.floor(value / increment) * increment);
				value = Math.min(Math.max(minValue, value), maxValue);

				sliderPos = (int) ((value - minValue) / (maxValue - minValue) * width);

				if (listener != null) {
					listener.onClick(value);
				}
			}
		}

		if (hasFocus && Input.isReleased("use_left")) {
			hasFocus = false;
			if (listener != null) {
				listener.onRelease(value);
			}
		}

		UI.drawImage("none", x + txtWidth, y + 4, width + 2, height - 8).setColor(Colors.GUI_BORDER_COLOR);
		UI.drawImage("gui_slider", x + txtWidth + sliderPos, y, 4, height).setDepth(-1);
		if (increment < 1) {
			UI.drawString(prefix + String.format("%.2f", value), x + txtWidth + width + 20, y - 6);
		} else {
			UI.drawString(prefix + String.format("%.0f", value), x + txtWidth + width + 20, y - 6);
		}
	}
}
