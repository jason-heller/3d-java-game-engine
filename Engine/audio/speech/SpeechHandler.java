package audio.speech;

public class SpeechHandler {
	private static SpeechToText speech;
	private static TextToSpeech text;
	private static Phrases phrases;
	
	public static void init() {
		phrases = new Phrases();
		
		speech = new SpeechToText();
		text = new TextToSpeech();
	}
	
	public static void cleanUp() {
		speech.cleanUp();
		text.cleanUp();
	}
	
	public static void speak(String say) {
		text.speak(say);
	}
	
	public static Phrase checkoutLastPhrase() {
		return phrases.getPhrase(speech.checkoutResult());
	}

	public static void start() {
		//speech.start();
		//text.start();
	}
	
	public static void stop() {
		//speech.stop();
		//text.stop();
	}
}
