package io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import audio.AudioHandler;
import audio.Source;
import core.Resources;
import dev.cmd.Console;
import gl.Window;

public class MusicHandler {
	
	private int numSongs;
	
	private boolean transition = false;
	private float volume;
	
	private Source source;
	
	private static int pseudoRandPos = 0;
	private static final int[] pseudoRandPattern = new int[] { 1, 2, -1, 2 };
	private static int pseudoRand = pseudoRandPos;

	public void loadAll() {
		try {

			List<String> files = findFiles("music", "ogg");
            files.forEach(x -> addSong(x));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void loadQueue(int numQueue) {
		
		try {

			List<String> files = findFiles("music", "ogg");
            int numFiles = files.size();
            
            if (numFiles < numQueue) {
            	return;
            }
			
			for(int i = 0; i < numQueue; i++) {
				int rand = getPseudoRandom(numFiles);
				addSong(files.get(rand));
				numFiles--;
				files.remove(rand);
			}
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private int getPseudoRandom(int numFiles) {
		int pattern = pseudoRandPattern[pseudoRandPos++ % pseudoRandPattern.length];
		pseudoRand += pattern;
		return (pseudoRand) % numFiles;
	}

	private void addSong(String fileName) {
		Resources.addSound("music" + numSongs, fileName.substring(fileName.indexOf("music\\")));
		numSongs++;
	}
	
	public void cleanUp() {
		for(int i = 0; i < numSongs; i++) {
			Resources.removeSound("music" + i);
		}
	}
	
	private static List<String> findFiles(String pathName, String fileExtension) throws IOException {
		Path path = Paths.get(FileUtils.WORKING_DIRECTORY + "/src/res/sfx/" + pathName);
		
		if (!Files.isDirectory(path))
			throw new IllegalArgumentException("Path must be a directory : " + path.toString());

		List<String> result;

		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> !Files.isDirectory(p)).map(p -> p.toString().toLowerCase())
					.filter(f -> f.endsWith(fileExtension)).collect(Collectors.toList());
		}

		return result;
	}

	public void playNext() {
		if (numSongs == 0) 
			return;
		
		if (volume == 0f) {
			source = AudioHandler.playMusic("music" + (numSongs - 1));
			source.setGain(1f);
			volume = 1f;
			numSongs--;
		} else {
			transition = true;
		}
	}
	
	public void stopMusic() {
		AudioHandler.stop("music" + (numSongs));
	}

	public void update() {
		if (transition) {
			volume -= Window.deltaTime;
			source.setGain(volume);
			
			if (volume <= 0f) {
				volume = 0f;
				transition = false;
				playNext();
			}
		}
	}
}
