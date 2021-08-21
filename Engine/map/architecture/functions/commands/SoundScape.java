package map.architecture.functions.commands;

import org.joml.Vector3f;

import audio.AudioHandler;
import dev.cmd.Console;
import map.architecture.functions.ArcFunction;

public class SoundScape extends ArcFunction {
	private String sound;
	private static String lastSound = null;
	
	public SoundScape(Vector3f pos, String sound) {
		super("soundscape", pos);
		this.sound = sound;
	}

	@Override
	public void trigger(String[] args) {
		if (lastSound != null) {
			AudioHandler.stop(lastSound);
		}
		
		AudioHandler.loop(sound);
		lastSound = sound;
		
		Console.log(sound, "soundscape");
	}
}