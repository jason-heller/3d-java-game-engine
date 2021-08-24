package audio.speech;

import static audio.speech.Words.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.cmd.Console;

public class Phrases {
	private static Map<Phrase, List<Words[]>> phraseMap;
	
	public Phrases() {
		phraseMap = new HashMap<>();
		
		addPhrase(Phrase.LOCATION_INQUIRY, ARE, YOU, HERE);
		addPhrase(Phrase.LOCATION_INQUIRY, WHERE, ARE, YOU);
		addPhrase(Phrase.ANTAGONIZE, SHOW, YOURSELF);
		addPhrase(Phrase.ANTAGONIZE, SHOW, US);
		addPhrase(Phrase.ANTAGONIZE, REVEAL, YOURSELF);
		addPhrase(Phrase.TEST, TEST);
	}
	
	public void addPhrase(Phrase name, Words ...words) {
		List<Words[]> sentenceList = phraseMap.get(name);
		if (sentenceList == null) {
			sentenceList = new ArrayList<>();
			phraseMap.put(name, sentenceList);
		}
		
		sentenceList.add(words);
	}
	
	public Phrase getPhrase(String result) {
		String[] resultWords = result.split(" ");
		for(Phrase phrase : phraseMap.keySet()) {
			List<Words[]> sentences = phraseMap.get(phrase);
			for(Words[] phraseWords : sentences) {
				if (phraseWords.length != resultWords.length)
					continue;
				
				int i = 0;
				for(Words word : phraseWords) {

					if (!word.toString().equals(resultWords[i])) {
						break;
					}
					
					i++;
				}
				
				if (i == phraseWords.length) {
					return phrase;
				}
			}
		}
		
		return null;
	}
}
