package scene.entity.util;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import dev.Debug;
import geom.MTV;
import gl.Camera;
import gl.Window;
import gl.post.PostProcessing;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import ui.UI;

/**
 * @author Jason
 *
 */
public class PlayerEntity extends PhysicsEntity {
	
	private Camera camera;
	
	private static int hp = 15;
	private static int maxHp = 15;
	
	private float invulnTimer = 0f;
	private float stepTimer = 0f;;
	
	private static final float INVULN_TIME = 2f;
	
	private float bloodDmgIndicator = 0f;
	
	public PlayerEntity(Camera camera) {
		super("player", new Vector3f(3.5f, 7f, 3.5f));
		this.camera = camera;
		PlayerHandler.setEntity(this);
		
	}
	
	@Override
	public void update(PlayableScene scene) {

		PlayerHandler.update(Application.scene);
		super.update(scene);
		
		if (submerged) {
			fullySubmerged = scene.getCamera().getPosition().y < leaf.max.y;
			PostProcessing.underwater = this.isFullySubmerged();
		} else {
			PostProcessing.underwater = false;
		}
		
		if (grounded) {
			
			stepTimer += Window.deltaTime * new Vector3f(vel.x, 0f, vel.z).length();
			
			if (stepTimer > 6) {
				stepTimer = 0f;
				
				String sfx;
				switch(materialStandingOn) {
				case GRASS:
					sfx = "walk_grass";
					break;
				case DIRT:
					sfx = "walk_dirt";
					break;
				case MUD:
					sfx = "walk_mud";
					break;
				default:
					sfx = "walk_rock";
				}
				AudioHandler.play(sfx);
			}
		}
		
		invulnTimer = Math.max(invulnTimer - Window.deltaTime, 0f);
		
		if (hp < 5 || bloodDmgIndicator > 0f) {
			float baseDmgOpaciy = Math.max(Math.min(bloodDmgIndicator, 1f), (5f - hp) / 5f);
			UI.drawImage("dmg_screen_effect", 0, 0, 1280, 720).setOpacity(baseDmgOpaciy);
			bloodDmgIndicator -= Window.deltaTime;
		}
		
		if (hp <= 0) {
			
			PlayerHandler.disable();
			if (camera.getControlStyle() == Camera.FIRST_PERSON) {
				camera.getPosition().set(pos.x, pos.y + Camera.offsetY, pos.z);
			}
			
			if (Camera.offsetY > -3f) {
				Camera.offsetY -= 3f*Window.deltaTime;
			}
			
			
			
			camera.sway(1f, 4f, .45f);
		}
	}
	
	/** Hurts the player
	 * @param damage - how much damage the player will take (if negative, it will choose from 0 to |damage|)
	 * @param part - the part (0 = head, 1 = arm, 2 = hip, 3 = leg)
	 */
	public void takeDamage(int damage) {
		if (Debug.god) return;
		if ((invulnTimer != 0f && invulnTimer != INVULN_TIME) || hp <= 0)
			return;

		if (damage < 0)
			damage = (int)(Math.random() * (-damage));
		
		camera.flinch(camera.getDirectionVector().negate(), damage * 10);
		
		hp = Math.max(hp - damage, 0);
		bloodDmgIndicator = 1f;
		invulnTimer = INVULN_TIME;
	}

	public void heal(int health) {
		hp += health;
		
		if (hp > 0) {
			PlayerHandler.enable();
		}
	}
	
	@Override
	protected void collideWithFloor(Bsp bsp, MTV mtv) {
		/*float fallHeight = -PlayerHandler.jumpVel * 3.4f;
		if (vel.y < fallHeight) {
			takeDamage((int) (-vel.y / 20f));
			AudioHandler.play("fall");

			vel.y = -PlayerHandler.jumpVel;	// TODO: Bad
		}*/
		
		super.collideWithFloor(bsp, mtv);
	}

	public static int getHp() {
		return hp;
	}
	
	public static int getMaxHp() {
		return maxHp;
	}

	public void reset() {
		hp = maxHp;
	}
}
