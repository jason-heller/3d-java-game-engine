package audio.recognition;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;

import dev.Console;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import gl.Window;

public class SpeechRecognizer {
	private static Configuration config;
	private static LiveSpeechRecognizer recognizer;

	private static boolean doProcess = false;
	private static boolean cleanUp = false;
	
	private static SpeechResult speechResult;

	private static Thread speechThread;
	private static Thread resourcesThread;
	
	private static float micTestTimer = 0f;
	
	private static String lastHypoth = "<unk>";

	public static void init() {
		config = new Configuration();
		config.setAcousticModelPath("edu/cmu/sphinx/models/en-us/en-us");
		//config.setDictionaryPath("edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		config.setDictionaryPath("src/res/lang/en-us/en-us.dict");
		//config.setLanguageModelPath("edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		config.setGrammarPath("src/res/lang/en-us");
		config.setGrammarName("grammar");
		config.setUseGrammar(true);

		try {
			recognizer = new LiveSpeechRecognizer(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		recognizer.startRecognition(true);
		startSpeechThread();
		startResourcesThread();
	}
	
	public static void start() {
		doProcess = true;
	}
	
	public static void stop() {
		doProcess = false;
	}
	
	public static void cleanUp() {
		cleanUp = true;
		doProcess = false;
	}

	public static void startSpeechThread() {

		if (speechThread != null && speechThread.isAlive())
			return;

		speechThread = new Thread(() -> {

			try {

				Console.log("listening..");
				while (!cleanUp) {
					
					speechResult = recognizer.getResult();

					if (speechResult == null) {
						Console.log("Couldnt recognize speech");
					} else {
						lastHypoth = speechResult.getHypothesis();
						//speechResult.getWords().get(0).getConfidence();

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Console.log("no longer listening");
			recognizer.stopRecognition();
		});

		speechThread.start();
	}

	public static void startResourcesThread() {

		if (resourcesThread != null && resourcesThread.isAlive())
			return;

		resourcesThread = new Thread(() -> {
			while (!cleanUp) {

				if (!doProcess)
					continue;
				
				if (micTestTimer > .35f) {
					if (AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
					} else {
	
					}
					micTestTimer = 0f;
				}
				
				micTestTimer += Window.deltaTime;
			}
		});

		resourcesThread.start();
	}

	public static String getLastHypothesis() {
		return lastHypoth;
	}

	public static void clearLastHypothesis() {
		lastHypoth = "<unk>";
	}
}
