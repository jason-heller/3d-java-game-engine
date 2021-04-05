package scene.singlearc;

import java.util.Iterator;
import java.util.LinkedList;

import org.joml.Vector3f;

import gl.Window;
import scene.entity.util.PlayerEntity;
import ui.Image;
import ui.UI;
import util.Colors;

public class DamageIndicators {
	protected static final float DMG_TIMER_START = 8f;

	private LinkedList<DamagePart> dmgIndicatorPart = new LinkedList<>();

	private float bloodScreenFxTimer = 0f;
	private float slashTimer = 0f;
	
	public DamageIndicators() {
	}

	public void update() {
		
		if (slashTimer > 0f) {
			slashTimer = Math.max(slashTimer - Window.deltaTime, 0f);
			UI.drawImage("dmg_slash", 480, 200, 320, 320).setOpacity(slashTimer);
		}
		
		int ypos = 96;
		
		bloodScreenFxTimer = Math.max(bloodScreenFxTimer - Window.deltaTime, 0f);
		
		Iterator<DamagePart> iter = dmgIndicatorPart.iterator();
		while(iter.hasNext()) {
			DamagePart part = iter.next();
			if (part.timer == 0f) continue;
			
			part.timer = Math.max(part.timer - Window.deltaTime, 0f);
			
			float bumpEffect = Math.max(Math.max(0f, part.timer - 7.75f) * 7f, 1f);
			int scale = (int)(64 * bumpEffect);
			float opacity = Math.min(part.timer, 1f);
			
			Image img = UI.drawImage("damage_indicators", 96, 96 + ypos, scale, scale);
			img.setUvOffset(0f, .25f * part.id, .25f, .25f + .25f * part.id);
			img.setOpacity(opacity);
			
			if (bumpEffect == 1f) {
				UI.drawString("#r" + part.damage + "#w damage taken to #r" + part.toString(), (96 + 72), (96 + 32) + ypos, .25f, false)
				.setOpacity(opacity);
				UI.drawRect(96, 96 + ypos + 72, 64, 12, Colors.BLACK).setOpacity(opacity);
				float ratio = (PlayerEntity.getHp(part.id) / (float)PlayerEntity.getMaxHp(part.id));
				UI.drawRect(97, 97 + ypos + 72, (int)(ratio * 62), 10,
						determineColor(ratio)).setOpacity(opacity);
			}
			
			ypos += 96;
			
			if (part.timer == 0f) {
				iter.remove();
			}
		}
	}

	private Vector3f determineColor(float ratio) {
		if (ratio < .25f) {
			return Colors.RED;
		} else if (ratio < .5f) {
			return Colors.ORANGE;
		}
		
		return Colors.GOLD;
	}

	public void damageTaken(int part, int damage) {
		for(DamagePart dmgPart : dmgIndicatorPart) {
			if (dmgPart.id == part) {
				dmgPart.timer = DMG_TIMER_START;
				dmgPart.damage += damage;
				bloodScreenFxTimer = Math.min(.2f * damage, 1f);
				
				if (part == 0)
					slashTimer = 1f;
				
				return;
			}
		}
		
		dmgIndicatorPart.add(new DamagePart(part, damage));
		bloodScreenFxTimer = Math.min(.2f * damage, 1f);
		
		if (part == 0)
			slashTimer = 1f;
		
	}

	public float getBloodScreenTimer() {
		return bloodScreenFxTimer;
	}
}

class DamagePart {
	public int id;
	public float timer;
	public int damage;
	
	public DamagePart(int id, int damage) {
		this.id = id;
		this.damage = damage;
		timer = DamageIndicators.DMG_TIMER_START;
	}
	
	public String toString() {
		switch(id) {
		case 0:
			return "head";
		case 1:
			return "hands";
		case 2:
			return "hips";
		default:
			return "feet";
		}
	}
}