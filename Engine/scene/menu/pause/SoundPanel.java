package scene.menu.pause;

import audio.AudioHandler;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.SliderListener;

public class SoundPanel extends GuiPanel {
	private final GuiSlider masterVolume, musicVolume, sfxVolume;

	public SoundPanel(GuiPanel parent, float x, float y, float width, float height) {
		super(parent, x, y, width, height);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);

		add(new GuiLabel(x, y ,"#SVolume"));
		masterVolume = new GuiSlider(x, y, "Master Volume", 0f, 1f, AudioHandler.volume, .01f);
		masterVolume.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
				AudioHandler.volume = value;
				AudioHandler.changeMasterVolume();
			}

			@Override
			public void onRelease(float value) {
				AudioHandler.volume = value;
				AudioHandler.changeMasterVolume();
			}

		});
		add(masterVolume);
		addSeparator();
		
		sfxVolume = new GuiSlider(x, y, "Sound Volume", 0f, 1f, AudioHandler.sfxVolume, .01f);
		sfxVolume.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
				AudioHandler.sfxVolume = value;
				AudioHandler.changeMasterVolume();
			}

			@Override
			public void onRelease(float value) {
				AudioHandler.sfxVolume = value;
				AudioHandler.changeMasterVolume();
			}

		});
		add(sfxVolume);
		
		musicVolume = new GuiSlider(x, y, "Music Volume", 0f, 1f, AudioHandler.musicVolume, .01f);
		musicVolume.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
				AudioHandler.musicVolume = value;
				AudioHandler.changeMasterVolume();
			}

			@Override
			public void onRelease(float value) {
				AudioHandler.musicVolume = value;
				AudioHandler.changeMasterVolume();
			}

		});
		add(musicVolume);
	}
}
