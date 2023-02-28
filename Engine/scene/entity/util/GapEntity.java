package scene.entity.util;

import audio.AudioHandler;
import dev.cmd.Console;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;

public class GapEntity extends Entity {
	
	private int points;
	private String trigger1, trigger2;
	
	private boolean active = false;

	public GapEntity(String name, int points, String trigger1, String trigger2) {
		super(name);
		this.points = points;
		this.trigger1 = trigger1;
		this.trigger2 = trigger2;
	}
	
	@Override
	public void update(PlayableScene scene) {
		GapTrigger t1 = (GapTrigger) EntityHandler.getEntityByName(trigger1);
		
		if (t1 == null)
			return;
		
		GapTrigger t2 = (GapTrigger) EntityHandler.getEntityByName(trigger2);
		
		if (t2 == null)
			return;
		
		if (t1.isActive() && t2.isActive()) {
			if (!active) {
				
				PlayerEntity player = scene.getPlayer();
				GapTrigger lastActivated = getLastActivated(t1, t2);
				
				if (lastActivated.requiresGrind() && !player.isGrinding())
					return;
				
				//if (lastActivated.requiresLanding() && !player.isGrounded())
				//	return;
					
				triggerGap(player);
			}
		} else {
			active = false;
		}
	}

	private void triggerGap(PlayerEntity player) {
		active = true;
		player.getTrickManager().addTrick("#b" + name, points, .1f);
		AudioHandler.play("gap");
	}

	private GapTrigger getLastActivated(GapTrigger t1, GapTrigger t2) {
		return t1.getActivationTime() > t2.getActivationTime() ? t1 : t2;
	}

	public boolean isActive() {
		return active;
	}
}
