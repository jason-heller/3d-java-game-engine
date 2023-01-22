package gl.anim;

import gl.anim.component.Keyframe;

public class Animation {

	private final float duration; // seconds
	private final Keyframe[] frames;
	private final int numJoints;
	private final boolean isLooping, noTransition;
	private final String name;
	private final String nextAnim;

	public Animation(String name, float duration, Keyframe[] frames, int numJoints, boolean isLooping, boolean noTransition, String nextAnim) {
		this.name = name;
		this.frames = frames;
		this.duration = duration;
		this.numJoints = numJoints;
		this.isLooping = isLooping;
		this.noTransition = noTransition;
		this.nextAnim = nextAnim;
	}

	public float getDuration() {
		return duration;
	}

	public Keyframe[] getKeyframes() {
		return frames;
	}
	
	public int getNumJoints() {
		return numJoints;
	}
	
	public boolean hasNoTransition() {
		return noTransition;
	}
	
	public boolean isLooping() {
		return this.isLooping;
	}
	
	public String getNextAnim() {
		return nextAnim;
	}

	public String getName() {
		return name;
	}
}
