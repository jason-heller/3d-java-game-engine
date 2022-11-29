package gl.anim;

import gl.anim.component.Keyframe;

public class Animation {

	private final float duration; // seconds
	private final Keyframe[] frames;
	private final int numJoints;

	public Animation(float duration, Keyframe[] frames, int numJoints) {
		this.frames = frames;
		this.duration = duration;
		this.numJoints = numJoints;
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
}
