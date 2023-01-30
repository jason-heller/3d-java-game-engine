package scene.menu.pause;

import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import core.App;
import io.Input;
import ui.Text;
import ui.UI;
import util.Vectors;

public class PauseGui {
	public PausePanel pause;
	private boolean isPaused = false, stopTime = true;
	private final Text title;
	private boolean canPause = true;

	public PauseGui() {
		pause = new PausePanel(this);
		pause.setFocus(true);

		title = new Text("PAUSED", 100, 100, 1f, false);
	}

	public boolean isPaused() {
		return this.isPaused;
	}

	public void pause() {
		Input.requestMouseRelease();
		if (stopTime) {
			App.paused = true;
			AudioHandler.pause();
		}
		isPaused = true;
	}

	public void setPausable(boolean canPause) {
		this.canPause = canPause;
	}

	public void setTimeFreezeOnPause(boolean stopTime) {
		this.stopTime = stopTime;
	}

	public void unpause() {
		Input.requestMouseGrab();
		if (stopTime) {
			App.paused = false;
			AudioHandler.unpause();
		}
		pause.collapse();

		isPaused = false;
	}

	public void update() {
		if (Input.isPressed(Keyboard.KEY_ESCAPE) && canPause) {
			if (!isPaused()) {
				pause();

			} else {
				if (pause.isFocused()) {
					unpause();
				} else {
					pause.collapse();
				}
			}
		}

		if (isPaused()) {
			UI.drawRect(0, 0, UI.width, UI.height, Vectors.ZERO).setOpacity(.35f);
			pause.draw();
			UI.drawString(title);
		}
	}
}
