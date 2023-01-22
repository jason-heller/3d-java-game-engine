package ui.menu.layout;

import ui.menu.GuiElement;

public interface GuiLayout {

	public void addSeparator();

	public float getHeight();

	public float getWidth();

	public float getX();

	public float getY();

	public void init(float x, float y, float w, float h);

	public void newElement(GuiElement element);

	public void reset();
}