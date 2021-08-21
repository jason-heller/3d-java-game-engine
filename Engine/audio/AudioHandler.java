package audio;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCcontext;
import org.lwjgl.openal.ALCdevice;
import org.lwjgl.openal.EFX10;
import org.lwjgl.util.WaveData;
import org.newdawn.slick.openal.OggData;
import org.newdawn.slick.openal.OggDecoder;

import core.Resources;
import gl.Camera;
import io.FileUtils;

public class AudioHandler {

	private static Map<SoundEffects, SoundEffect> effects = new HashMap<SoundEffects, SoundEffect>();
	private static Map<SoundFilters, Integer> filters = new HashMap<SoundFilters, Integer>();
	
	private static final int MAX_SOURCES = 80;
	private static final int MAX_STATIC_SOURCES = 32;
	private static final int MAX_LOOPING_SOURCES = 8;
	private static Source[] sources = new Source[MAX_SOURCES];
	private static int sourcePtr = 0, sourceLoopPtr = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES, sourceCheckoutPtr = MAX_STATIC_SOURCES;
	
	public static float volume = 0.5f, sfxVolume = 1.0f, musicVolume = 1.0f;
	
	public static Source play(String sound) {
		Source src = sources[sourcePtr++];
		src.play(sound);
		if (sourcePtr == MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES)
			sourcePtr = 0;
		return src;
	}
	
	public static Source checkoutSource() {
		Source src = sources[sourceCheckoutPtr++];
		if (sourcePtr == MAX_SOURCES)
			sourcePtr = MAX_STATIC_SOURCES;
		return src;
	}
	
	public static Source loop(String sound) {
		Source src = sources[sourceLoopPtr++];
		src.play(sound);
		if (sourceLoopPtr == MAX_STATIC_SOURCES)
			sourceLoopPtr = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES;
		return src;
	}
	
	public static Source playMusic(String sound) {
		Source src = sources[sourceLoopPtr++];
		src.playAsMusic(sound);
		if (sourceLoopPtr == MAX_STATIC_SOURCES)
			sourceLoopPtr = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES;
		return src;
	}

	public static void changeMasterVolume() {
		for (final Source source : sources) {
			source.update();
		}
	}

	public static void cleanUp() {

		for(Source source : sources) {
			source.delete();
		}

		for (final SoundEffect sfx : getEffects().values()) {
			EFX10.alDeleteEffects(sfx.getId());
			EFX10.alDeleteAuxiliaryEffectSlots(sfx.getSlot());
		}

		for (final int filter : getFilters().values()) {
			EFX10.alDeleteFilters(filter);
		}
		
		Resources.removeAllSounds();

		AL.destroy();
	}

	public static Map<SoundEffects, SoundEffect> getEffects() {
		return effects;
	}

	public static Map<SoundFilters, Integer> getFilters() {
		return filters;
	}
	
	public static void init() {
		try {
			AL.create();
			AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
			setupEFX();
			setupEffects();
			setupFilters();
			Thread.sleep(50);
			
			int i = 0;
			for(; i < MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES; i++) {
				sources[i] = new Source();
			}
			for(; i < MAX_STATIC_SOURCES; i++) {
				sources[i] = new Source();
				sources[i].setLooping(true);
			}
			for(; i < MAX_SOURCES; i++) {
				sources[i] = new Source();
			}
			
		} catch (final LWJGLException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static int loadOgg(String path) {
		try {
			final int buffer = AL10.alGenBuffers();
			final InputStream in = FileUtils.getInputStream(path);
			final OggDecoder decoder = new OggDecoder();
			final OggData ogg = decoder.getData(in);

			AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, ogg.data, ogg.rate);

			return buffer;
		} catch (final Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	public static int loadWav(String path) {
		final int buffer = AL10.alGenBuffers();
		final WaveData waveFile = WaveData.create(path);
		AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		return buffer;
	}

	public static void pause() {
		for (int i = 0; i < sources.length; i++) {
			final Source s = sources[i];
			if (s.isPlaying()) {
				s.pause();
			}
		}
	}

	private static void setupEffects() {
		int effect, slot;

		// Echo
		effect = EFX10.alGenEffects();
		slot = EFX10.alGenAuxiliaryEffectSlots();
		effects.put(SoundEffects.ECHO, new SoundEffect(effect, slot));

		EFX10.alEffecti(effect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_ECHO);
		//EFX10.alEffectf(effect, EFX10.AL_ECHO_DELAY, 5.0f);
		EFX10.alAuxiliaryEffectSloti(slot, EFX10.AL_EFFECTSLOT_EFFECT, effect);
		
		effect = EFX10.alGenEffects();
		slot = EFX10.alGenAuxiliaryEffectSlots();
		effects.put(SoundEffects.REVERB, new SoundEffect(effect, slot));

		EFX10.alEffecti(effect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_REVERB);
		EFX10.alEffectf(effect, EFX10.AL_REVERB_DECAY_TIME, 2.0f);
		EFX10.alAuxiliaryEffectSloti(slot, EFX10.AL_EFFECTSLOT_EFFECT, effect);
	}

	private static void setupEFX() throws Exception {
		final ALCdevice device = AL.getDevice();
		// String defaultDeviceName = ALC10.alcGetString(device,
		// ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);

		final ALCcontext newContext = ALC10.alcCreateContext(device, (IntBuffer) null);
		if (newContext == null) {
			throw new Exception("Failed to create context");
		}
		final int contextCurResult = ALC10.alcMakeContextCurrent(newContext);
		if (contextCurResult == ALC10.ALC_FALSE) {
			throw new Exception("Failed to make context");
		}
	}

	private static void setupFilters() throws Exception {
		int filter;

		// Low Pass Freq
		filter = EFX10.alGenFilters();
		filters.put(SoundFilters.LOW_PASS_FREQ, filter);

		EFX10.alFilteri(filter, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);
		EFX10.alFilterf(filter, EFX10.AL_LOWPASS_GAIN, 0.5f);
		EFX10.alFilterf(filter, EFX10.AL_LOWPASS_GAINHF, 0.5f);
		
		filter = EFX10.alGenFilters();
		filters.put(SoundFilters.LOW_PASS_FILTER, filter);
		EFX10.alFilteri(filter, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);
		EFX10.alFilterf(filter, EFX10.AL_LOWPASS_GAIN, 0.5f);
		EFX10.alFilterf(filter, EFX10.AL_LOWPASS_GAINHF, 0.5f);
	}

	public static void underwater(boolean submerged) {
		if (submerged) {
			for (final Source s : sources) {
				s.applyFilter(SoundFilters.LOW_PASS_FREQ);
			}
		} else {
			for (final Source s : sources) {
				s.removeFilter();
			}
		}

	}

	public static void unpause() {
		for (final Source s : sources) {
			// s.removeEffect();
			s.unpause();
		}
	}

	public static void update(Camera camera) {
		final Vector3f p = camera.getPosition();
		AL10.alListener3f(AL10.AL_POSITION, p.x, p.y, p.z);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
	}

	public static void stop(String sound) {
		for(int i = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES; i < MAX_STATIC_SOURCES; i++) {
			if (sources[i].getSound().equals(sound)) {
				sources[i].stop();
			}
		}
	}

	public static void stopAll() {
		for(int i = 0; i < MAX_STATIC_SOURCES; i++) {
			sources[i].stop();
		}
	}

}
