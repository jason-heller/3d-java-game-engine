package ui.menu.layout;

import ui.menu.GuiElement;

public class GuiRowLayout implements GuiLayout {

	private int x, y, w, h, xo, yo;
	private int dx, dy;
	private int rowWidth, rowHeight;

	private int padding = 10;

	public GuiRowLayout(int rowWidth, int rowHeight) {
		this.rowWidth = rowWidth;
		this.rowHeight = rowHeight;
	}

	@Override
	public void addSeparator() {
		dy += rowHeight;
		dx = x;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void init(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.dx = x;
		this.dy = y;
		this.xo = x;
		this.yo = y;
	}

	@Override
	public void reset() {
		this.x = xo;
		this.y = yo;
		this.dx = xo;
		this.dy = yo;
	}
	
	@Override
	public void newElement(GuiElement element) {
		element.setPosition(dx, dy);
		
		dx += Math.max(element.width, rowWidth) + padding;

		if (dx > x + w) {
			nextRow();
		}

	}

	public void nextRow() {
		dy += rowHeight + padding;
		dx = x;
	}

	public void setPadding(int p) {
		padding = p;
	}

}
