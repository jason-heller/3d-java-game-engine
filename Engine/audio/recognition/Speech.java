package audio.recognition;

import dev.cmd.Console;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class Speech {

	private Thread speechThread;
	private Microphone microphone;
	private Recognizer recognizer;

	private static String resultText = "<unk>";
	
	public void start() {
		if (speechThread != null && speechThread.isAlive())
			return;

		startSpeechThread();
	}

	public void stop() {
		if (speechThread == null || speechThread.isAlive())
			return;

		recognizer.deallocate();
		microphone.stopRecording();
	}

	private void startSpeechThread() {
		//Console.log("starting speech thread");
		System.gc();
		speechThread = new Thread(() -> {
			ConfigurationManager cm;
			cm = new ConfigurationManager(Speech.class.getResource("config.xml"));
			recognizer = (Recognizer) cm.lookup("recognizer");
			Console.log("allocate",System.currentTimeMillis());

			recognizer.allocate();
			Console.log("done",System.currentTimeMillis());
			microphone = (Microphone) cm.lookup("microphone");
			if (!microphone.startRecording()) {
				Console.log("Cannot start microphone");
				recognizer.deallocate();
				return;
			}

			while (true) {

				Result result = recognizer.recognize();

				if (result != null) {
					resultText = result.getBestFinalResultNoFiller();
					Console.log("Interpreted voice: \"" + resultText + "\"");
				} else {
					Console.log("Couldn't hear");
					resultText = "<unk>";
				}
			}
		});
		
		speechThread.start();
	}
	
	public static String getResultText() {
		return resultText;
	}

	public static void clearResultText() {
		resultText = "<unk>";
	}
}
