package audio.speech;

public enum Words {
	// VERBS
	IS, ARE, AM,
	
	// PROPER PRONOUNS
	ME, YOU, WE, THEY, YOURSELF, US,
	
	// LOCATIVE
	HERE, THERE,
	
	// QUESTIONS
	WHO, WHAT, WHERE, WHEN, WHY,
	
	// ADJECTIVES
	DEAD, ALIVE, GHOST, SPIRIT,
	
	// VERBS
	KILL, HATE, HAUNT, SHOW, REVEAL,
	GOING,
	
	// NOUNS
	FEAR,
	
	// ETC
	TO, THE, A, AN, TEST,
	FUCK, SHIT;

	public static String getWordList() {
		String wordList = "";
		
		final int len = values().length - 1;
		for(int i = 0; i < len; i++) {
			String word = values()[i].toString();
			wordList += word + " ";
		}
		
		wordList += values()[len];
		return wordList;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
