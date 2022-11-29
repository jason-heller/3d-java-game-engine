package scene.entity.util;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public enum LightStyle {
	STATIC("m"),
	
	FLICKER("mmamammmmammamamaaamammma"),
	
	SLOW_PULSE("jklmnopqrstuvwxyzyxwvutsrqponmlkj");

	public static final int ANIMATION_SPEED_MS = 100;
	public static final FloatBuffer DEFAULT_LIGHTING = BufferUtils.createFloatBuffer(9);
	
	private LightCondition condition;
	private String[] brightnessPattern;
	
	private LightStyle(String pattern) {
		this.condition = LightCondition.NONE;
		this.brightnessPattern = new String[] {pattern};
	}
	
	private LightStyle(LightCondition condition, String...patterns) {
		this.condition = condition;
		this.brightnessPattern = patterns;
	}
	
	public LightCondition getLightCondition() {
		return condition;
	}
	
	public String getBrightnessPattern(int pattern) {
		return brightnessPattern[pattern];
	}
	
	public static LightStyle getStyleFromId(int id) {
		switch(id) {
		case 10:
			return FLICKER;
		case 5:
			return SLOW_PULSE;
		default:
			return STATIC;
		}
	}
}
