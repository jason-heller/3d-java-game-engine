package scene.menu.pause;

import gl.Camera;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.SliderListener;

public class GameplayPanel extends GuiPanel {
	
	private final GuiSlider sensitivity, cameraSway;
	
	public GameplayPanel(GuiPanel parent, float x, float y, float width, float height) {
		super(parent, x, y, width, height);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);

		add(new GuiLabel(x, y, "#SMouse"));
		sensitivity = new GuiSlider(x, y, "Mouse Sensitivity", .05f, 2f, Camera.mouseSensitivity, .05f);
		sensitivity.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Camera.mouseSensitivity = value;
			}

		});
		add(sensitivity);
		addSeparator();
		
		add(new GuiLabel(x, y, "#SCamera"));
		cameraSway = new GuiSlider(x, y, "Camera Sway", 0f, 1f, Camera.swayFactor, .01f);
		cameraSway.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Camera.swayFactor = value;
			}

		});
		add(cameraSway);
		
	}
}
