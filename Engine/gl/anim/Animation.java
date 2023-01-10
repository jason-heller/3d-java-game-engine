package gl.anim;

import gl.anim.component.Keyframe;

public class Animation {

	private final float duration; // seconds
	private final Keyframe[] frames;
	private final int numJoints;
	private final boolean isLooping;
	private final String nextAnim;

	public Animation(float duration, Keyframe[] frames, int numJoints, boolean isLooping, String nextAnim) {
		this.frames = frames;
		this.duration = duration;
		this.numJoints = numJoints;
		this.isLooping = isLooping;
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
	
	public boolean isLooping() {
		return this.isLooping;
	}
	
	public String getNextAnim() {
		return nextAnim;
	}
}
