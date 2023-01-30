package map.architecture.components;

import map.architecture.Architecture;
import scene.entity.Entity;

public class ArcTriggerClip extends ArcClip {

	public String commandEnter, commandExit;
	public String[] whitelist;
	public byte style;
	private boolean canFire = true;
	
	public static final byte SYLE_FIRE_ONCE = 0, STYLE_FIRE_MULTIPLE = 1;
	
	private Architecture arc;

	public ArcTriggerClip(Architecture arc) {
		this.arc = arc;
	}

	public boolean interact(Entity entity, boolean isEntering) {
		if (!canFire) return false;

		boolean allow = true;
		if (whitelist.length != 0 && !whitelist[0].equals("")) {
			allow = false;
			for(String name : whitelist) {
				if (entity.getName().equals(name)) {
					allow = true;
					break;
				}
			}
		}
		
		if (allow) {

			if (isEntering && !commandEnter.equals(""))
				arc.callCommand(entity, commandEnter);
			else if (!isEntering && !commandExit.equals(""))
				arc.callCommand(entity, commandExit);
			
			if (style == SYLE_FIRE_ONCE) {
				canFire = false;
			}
		}

		return false;
	}
}
