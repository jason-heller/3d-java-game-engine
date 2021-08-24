package audio.speech;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import dev.cmd.Console;

public class TextToSpeech {
	private Voice voice;
	private volatile boolean active;
	private volatile String speaking;
	private Thread ttsThread;
	
	private static final float DEFAULT_VOLUME = 1.0f;
	private static final float DEFAULT_PITCH = 50;
	private static final float DEFAULT_RATE = 140;
	
	public TextToSpeech() {
		voice = VoiceManager.getInstance().getVoice("kevin");	// Hii kevin
		if (voice != null) {
			voice.allocate();
			voice.setRate(DEFAULT_RATE);
			voice.setPitch(DEFAULT_PITCH);
			voice.setVolume(DEFAULT_VOLUME);
			voice.setPitchShift(1.0f);
			voice.setStyle("breathy");	// "business", "casual", "robotic", "breathy"
		}
	}
	
	public void varyPitch(float variance) {
		float deviation = (float)(((Math.random() - 0.5) * 2.0) * variance);
		voice.setPitch(DEFAULT_PITCH + deviation);
	}
	
	public void start() {
		if (active) {
			Console.warning("Tried to start tts thread while it was already active");
			return;
		}
		
		active = true;
		ttsThread = new Thread(() -> {
			while(active) {
				if (speaking == null)
					continue;
				voice.speak(speaking);
				speaking = null;
			}
		});
		ttsThread.start();
	}
	
	public void stop() {
		active = false;
	}

	public void cleanUp() {
		active = false;
		//while(ttsThread.isAlive()) {}
		voice.deallocate();
	}

	public void speak(String say) {
		speaking = say;
	}
	
	public Voice getVoice() {
		return voice;
	}
}
