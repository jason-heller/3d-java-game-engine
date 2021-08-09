package scene.viewmodel;

import org.joml.Matrix4f;

import audio.AudioHandler;
import gl.Window;
import gl.res.mesh.MeshData;

public class ViewModelSpiritBox extends ViewModel {

	private final int FREQ_MIN = 88;
	private final int FREQ_MAX = 108;

	private int freqMajor = 89;
	private int freqMinor = 5;
	private float freqTimer = 0f;
	private boolean freqInc = true;
	private float freqFluctuation = .1f;

	public ViewModelSpiritBox() {
		super("spiritbox", "default", new Matrix4f());
		MeshData.setField("freq", freqMajor + "." + freqMinor + "FM");
	}
	
	@Override
	public void equip() {
	}
	
	@Override
	public void holster() {
		AudioHandler.stop("spiritbox");
	}

	@Override
	public void update() {
		freqTimer += Window.deltaTime;

		if (freqTimer > freqFluctuation) {
			if (freqInc) {
				freqMinor += 2;
				if (freqMajor >= FREQ_MAX) {
					freqInc = false;
				} else if (freqMinor > 9) {
					freqMinor = 1;
					freqMajor++;
				}
			} else {
				freqMinor -= 2;
				if (freqMajor <= FREQ_MIN) {
					freqInc = true;
				} else if (freqMinor < 1) {
					freqMinor = 9;
					freqMajor--;
				}
			}

			if (Math.random() < .025) {
				freqFluctuation = 3f;
			} else if (freqFluctuation != .1f) {
				freqFluctuation = .1f;
				freqInc = !freqInc;
			}
			MeshData.setField("freq", freqMajor + "." + freqMinor + "FM");
			freqTimer = 0;
		}
	}
}
