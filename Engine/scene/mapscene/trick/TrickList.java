package scene.mapscene.trick;

import static scene.mapscene.trick.TrickType.GRIND_TRICK;
import static scene.mapscene.trick.TrickType.KICK_TRICK;

import java.util.HashMap;
import java.util.Map;

public class TrickList {
	private static Map<TrickType, Map<Integer, Trick>> tricks = new HashMap<>();
	
	public static void init() {
		for(TrickType type : TrickType.values()) {
			tricks.put(type, new HashMap<>());
		}
		
		// TODO: Load this from file
		addTrick(KICK_TRICK, "ollie", "ollie", 50, 0, "ollie");
		addTrick(KICK_TRICK, "kickflip", "kickflip", 250, 0, "flip");
		addTrick(KICK_TRICK, "pop shuvit", "popshuv", 300, 0, "flip+down").landBackwardsFlag();
		addTrick(GRIND_TRICK, "50-50", "5050", 300, 0, "grind");
		addTrick(GRIND_TRICK, "tailslide", "tailslide", 400, 0, "grind+down");
	}

	private static Trick addTrick(TrickType type, String name, String animation, int points, int safeFrame, String controls) {
		int flags = getControlFlags(controls);
		Trick trick = new Trick(type, name, animation, points, safeFrame);
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
					+ (control.equals("air") ? 128 : 0)
					+ (control.equals("modifier_l") ? 256 : 0)
					+ (control.equals("modifier_r") ? 512 : 0);
		}
		
		return flags;
	}
	
	public static Trick getTrick(TrickType type, int controls) {
		return tricks.get(type).get(controls);
	}
}
