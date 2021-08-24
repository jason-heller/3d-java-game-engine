package audio.speech;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import dev.cmd.Console;

public class SpeechToText {

	private TargetDataLine microphone;
	private Recognizer recognizer;
	
	private volatile boolean active = false;

	private volatile String lastResult = "";
	
	private static final String UNKNOWN = ""; //[unk]
	
	public SpeechToText() {
		
		LibVosk.setLogLevel(LogLevel.WARNINGS);

		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 44100, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		Model model = new Model("dll/vosk-model-small-en-us-0.15");
		String grammarLine = "[\"" + Words.getWordList() + "\", \"" + UNKNOWN + "\"]";
		recognizer = new Recognizer(model, 120000, grammarLine);

		try {
			microphone = (TargetDataLine) AudioSystem.getLine(info);
			microphone.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		if (active) {
			Console.warning("Tried to start speech thread while it was already active");
			return;
		}
		
		active = true;
		microphone.start();
		new Thread(() ->  {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        int numBytesRead;
	        int CHUNK_SIZE = 1024;
	        
			byte[] b = new byte[4096];

	        while (active) {
	        	numBytesRead = microphone.read(b, 0, CHUNK_SIZE);
	            out.write(b, 0, numBytesRead);

	            if (recognizer.acceptWaveForm(b, numBytesRead)) {
	            	lastResult = getLastValue(recognizer.getResult());
	            }
	        }
		}).start();
	}
	
	private String getLastValue(String result) {
		String read = "";
		boolean inQuotes = false;
		
		for(int i = result.length() - 2; i != 0; i--) {
			char c = result.charAt(i);
			if (c == '\"') {
				inQuotes = !inQuotes;
				
				if (!inQuotes)
					break;
			} else if (inQuotes) {
				read = c + read;
			}
		}
		return read;
	}

	public void stop() {
		active = false;
		microphone.stop();
		microphone.drain();
	}

	public void cleanUp() {
		stop();
		microphone.close();
		recognizer.close();
	}

	public String checkoutResult() {
		String result = lastResult;
		lastResult = "";
		return result;
	}
}
