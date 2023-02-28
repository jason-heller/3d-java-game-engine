package scene.mapscene.trick;

import static scene.mapscene.trick.TrickType.*;

import java.util.HashMap;
import java.util.Map;

public class TrickList {
	private static Map<TrickType, Map<Integer, Trick>> tricks = new HashMap<>();
	
	private static int numTricks;
	
	public static void init() {
		for(TrickType type : TrickType.values()) {
			tricks.put(type, new HashMap<>());
		}
		
		// TODO: Load this from file
		addTrick(KICK_TRICK, "ollie", "ollie", 50, 0, "ollie");
		addTrick(KICK_TRICK, "kickflip", "kickflip", 250, 0, "flip");
		addTrick(KICK_TRICK, "heelflip", "heelflip", 250, 0, "flip+right");
		addTrick(KICK_TRICK, "pop shuvit", "popshuv", 300, 0, "flip+down").landBackwardsFlag();
		addTrick(KICK_TRICK, "varialflip", "varialflip", 400, 0, "flip+right+up").landBackwardsFlag();
		addTrick(KICK_TRICK, "fs 180", "fs_180", 300, 0, "ollie+up").landBackwardsFlag().landSwitchFlag();
		addTrick(KICK_TRICK, "bs 180", "bs_180", 300, 0, "ollie+down").landBackwardsFlag().landSwitchFlag();
		addTrick(GRIND_TRICK, "50-50", "5050", 300, 0, "grind");
		addTrick(GRIND_TRICK, "tailslide", "tailslide", 400, 0, "grind+left");
		addTrick(GRIND_TRICK, "boardslide", "boardslide", 400, 0, "grind+right");
		addTrick(GRIND_TRICK, "nosegrind", "nosegrind", 400, 0, "grind+up");
		addTrick(GRIND_TRICK, "5-0", "fiveo", 400, 0, "grind+down");
		addTrick(GRAB_TRICK, "indy", "indy", 600, 0, "grab");
		addTrick(GRAB_TRICK, "nosegrab", "nosegrab", 600, 0, "grab+up");
		addTrick(GRAB_TRICK, "judo", "judo", 600, 0, "grab+left+up");
	}

	private static Trick addTrick(TrickType type, String name, String animation, int points, int safeFrame, String controls) {
		int flags = getControlFlags(controls);
		Trick trick = new Trick(type, name, animation, points, safeFrame, numTricks++);
		tricks.get(type).put(flags, trick);
		
		return trick;
	}
	
	private static int getControlFlags(String controlStr) {
		String[] controls = controlStr.split("\\+");
		int flags = 0;
		
		for(String control : controls) {
			flags |= (control.equals("up") ? 1 : 0)
					+ (control.equals("down") ? 2 : 0)
					+ (control.equals("left") ? 4 : 0)
					+ (control.equals("right") ? 8 : 0)
					+ (control.equals("ollie") ? 16 : 0)
					+ (control.equals("flip") ? 32 : 0)
					+ (control.equals("grind") ? 64 : 0)
					+ (control.equals("grab") ? 128 : 0)
					+ (control.equals("modifier_l") ? 256 : 0)
					+ (control.equals("modifier_r") ? 512 : 0);
		}
		
		return flags;
	}
	
	public static Trick getTrick(TrickType type, int controls) {
		return tricks.get(type).get(controls);
	}

	public static Trick getTrick(String targetName) {
		for(Map<Integer, Trick> batch : tricks.values()) {
			for(Trick trick : batch.values()) {

				if (trick.getName().equals(targetName)) {
					return trick;
				}
			}
		}
		
		return null;
	}
}
