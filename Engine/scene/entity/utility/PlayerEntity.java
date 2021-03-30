package scene.entity.utility;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import dev.Debug;
import geom.MTV;
import gl.Camera;
import gl.Window;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.singlearc.DamageIndicators;
import ui.UI;

/**
 * @author Jason
 *
 */
public class PlayerEntity extends PhysicsEntity {
	
	private Camera camera;
	
	public static final int HP_HEAD = 0, HP_HAND = 1, HP_HIP = 2, HP_FOOT = 3, HP_ALL = -1;
	private static int[] hp = new int[] {15, 10, 10, 10};		// Head, Hand, Hip, Foot
	private static int[] maxHp = new int[] {15, 10, 10, 10};		// Head, Hand, Hip, Foot
	
	private float invulnTimer = 0f;
	private float caughtTimer = 0f;
	private float stepTimer = 0f;
	private float deteriorationTimer = 0f;
	private int lastRandDmg = 0;
	private DamageIndicators dmgIndicators = new DamageIndicators();
	
	private static final float INVULN_TIME = 2f;
	
	public PlayerEntity(Camera camera) {
		super("player", new Vector3f(5f, 5f, 5f));
		this.camera = camera;
		PlayerHandler.setEntity(this);
		EntityHandler.addEntity(this);
	}
	
	@Override
	public void update(PlayableScene scene) {
		PlayerHandler.update(Application.scene);
		super.update(scene);
		
		if (grounded) {
			
			stepTimer += Window.deltaTime * vel.length();
			
			if (stepTimer > 6) {
				stepTimer = 0f;
				
				String sfx;
				switch(materialStandingOn) {
				case GRASS:
					sfx = "walk_grass";
					break;
				default:
					sfx = "walk_rock";
				}
				AudioHandler.play(sfx);
			}
		}
		
		invulnTimer = Math.max(invulnTimer - Window.deltaTime, 0f);
		
		float bloodDmgIndicator = dmgIndicators.getBloodScreenTimer();
		if (hp[0] < 5 || bloodDmgIndicator > 0f) {
			float baseDmgOpaciy = Math.max(Math.min(bloodDmgIndicator, 1f), (5f - hp[0]) / 5f);
			UI.drawImage("dmg_screen_effect", 0, 0, 1280, 720).setOpacity(baseDmgOpaciy);
		}
		
		if (!PlayerHandler.hasWalker && vel.lengthSquared() > .1f) {
			if (deteriorationTimer >= 1f) {
				deteriorationTimer -= 1f;
				
				if (Math.random() < .1 && lastRandDmg > 3) {
					takeDamage(1, 2);
					lastRandDmg = 0;
				} else {
					lastRandDmg++;
				}
			}
			
			deteriorationTimer += Window.deltaTime;
		}

		dmgIndicators.update();
	}
	
	/** Hurts the player
	 * @param damage - how much damage the player will take (if negative, it will choose from 0 to |damage|)
	 * @param part - the part (0 = head, 1 = arm, 2 = hip, 3 = leg)
	 */
	public void takeDamage(int damage, int part) {
		if (Debug.god) return;
		if ((invulnTimer != 0f && invulnTimer != INVULN_TIME) || hp[HP_HEAD] <= 0 || hp[part] <= 0)
			return;

		if (damage < 0)
			damage = (int)(Math.random() * (-damage));
		
		camera.flinch(camera.getDirectionVector().negate(), damage * 10);
		
		hp[part] = Math.max(hp[part] - damage, 0);
		if (part == HP_HAND)
			PlayerHandler.hasWalker = false;
		
		dmgIndicators.damageTaken(part, damage);
		
		invulnTimer = INVULN_TIME;
	}

	public void heal(int health, int part) {
		if (part < 0) {
			for(int i = 0; i < hp.length; i++) {
				hp[part] += health;
			}
		} else {
			hp[part] += health;
		}
	}
	
	@Override
	protected void collideWithFloor(MTV mtv) {
		float fallHeight = -PlayerHandler.jumpVel * 2.4f;
		if (vel.y < fallHeight) {
			takeDamage((int) (-vel.y / 20f), 2);
			takeDamage((int) (-vel.y / 15f), 3);
			AudioHandler.play("fall");
		}
		
		super.collideWithFloor(mtv);
	}

	public static int getHp(int id) {
		return hp[id];
	}
	
	public static int getMaxHp(int id) {
		return maxHp[id];
	}
}
