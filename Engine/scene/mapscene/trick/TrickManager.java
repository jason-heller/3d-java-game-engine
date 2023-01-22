package scene.mapscene.trick;

import java.util.HashMap;
import java.util.Map;

import gl.Window;
import gl.anim.Animation;
import gl.anim.component.Joint;
import io.Input;
import scene.entity.util.PlayerEntity;
import scene.mapscene.MapScene;

public class TrickManager {

	private PlayerEntity player;
	private MapScene scene;
	
	private Trick bufferedTrick, lastBuffered;
	private Trick currentTrick;
	private boolean isBuffering = false;

	public static boolean hasInfiniteJumps = false;
	
	private float holdBufferTimer = 0f;
	private float trickTimer = 0f;
	private float grindCooldownTimer = 0f;
	private int lastControlBuffer = -1;
	
	private float hangtime = 0f;
	
	private Map<Trick, Integer> trickDegregation;
	
	private TrickUI trickUI;
	
	private int comboScore = 0;
	private float multiplier = .5f;
	
	private static final float MIN_GRIND_LEN = 10f * 11f;
	
	public TrickManager(MapScene scene, PlayerEntity player) {
		this.scene = scene;
		this.player = player;
		trickDegregation = new HashMap<>();
		
		trickUI = new TrickUI();
	}

	public void update() {
		final boolean grounded = player.isGrounded();
		final boolean grinding = player.isGrinding();
		
		if (!grounded)
			hangtime += Window.deltaTime;

		if (grinding) {
			trickUI.setTimer(3f);
		} else {
			grindCooldownTimer = Math.max(grindCooldownTimer - Window.deltaTime, 0f);
		}
		
		if (trickTimer > 0f) {
			trickTimer = Math.max(trickTimer - Window.deltaTime, 0f);
		}
		
		trickUI.update();

		final boolean TR_OLLIE = Input.isDown("tr_ollie"),
				TR_FLIP = Input.isDown("tr_flip"),
				TR_AIR = Input.isDown("tr_air"),
				TR_MODIFIER_LEFT = Input.isDown("tr_modifier_l"),
				TR_MODIFIER_RIGHT = Input.isDown("tr_modifier_r"),
				TR_GRIND = Input.isDown("tr_grind");
		
		int trickFlags = 0;
		Trick newTrick = null;

		if (holdBufferTimer != 0) {
			holdBufferTimer = Math.max(holdBufferTimer - Window.deltaTime, 0f);
		} else {
			trickFlags = (Input.isDown("up") ? 1 : 0)
					+ (Input.isDown("down") ? 2 : 0)
					+ (Input.isDown("left") ? 4 : 0)
					+ (Input.isDown("right") ? 8 : 0)
					+ (TR_OLLIE ? 16 : 0)
					+ (TR_FLIP ? 32 : 0)
					+ (TR_GRIND ? 64 : 0)
					+ (TR_AIR ? 128 : 0)
					+ (TR_MODIFIER_LEFT ? 256 : 0)
					+ (TR_MODIFIER_RIGHT ? 512 : 0);
			
			if (!isBuffering && !grinding && grounded && trickFlags != 0) {
				player.getAnimator().start("charge");
			}
			
			if (TR_FLIP || TR_OLLIE) {
				if (grounded) {
					newTrick = TrickList.getTrick(TrickType.KICK_TRICK, trickFlags);
				}
			}
			
			if (TR_GRIND && !grinding && grindCooldownTimer == 0f) {
				Trick trick = TrickList.getTrick(TrickType.GRIND_TRICK, trickFlags);
				
				if (!grinding && trick != null) {
					player.startGrind();
					
					if (player.isGrinding()) {
						bufferedTrick = trick;
						startTrick();
						grindCooldownTimer = .2f;
					}
					
				}
			}
			
			/*if ((grounded || grinding || hasInfiniteJumps) && trickFlags == TRICK_OLLIE) {
				bufferedTrick = TrickList.getTrick(TrickType.KICK_TRICK, TRICK_OLLIE);
			}*/
			isBuffering = grounded && (TR_FLIP || TR_OLLIE || TR_AIR);
			
			if (lastBuffered != newTrick) {
				if (trickFlags < lastControlBuffer) {
					holdBufferTimer = .1f;
				} else {
					lastBuffered = bufferedTrick;
					bufferedTrick = newTrick;
				}
				
				lastControlBuffer = trickFlags;
			}
			
			if (trickFlags == 0 && bufferedTrick != null) {
				startTrick();
			}
		}
		
		if (bufferedTrick != null) {
			trickUI.drawBufferedTrick(bufferedTrick);
		}
	}

	private void degradeTrick(Trick trick) {
		Integer value = trickDegregation.get(trick);
		
		if (value == null) {
			trickDegregation.put(trick, 1);
		} else {
			int multiplier = value.intValue() + 1;
			trickDegregation.put(trick, multiplier);
		}
	}

	public void handleOnTrickEnd() {
		if (currentTrick == null)
			return;

		int points = getPoints(currentTrick);
		
		comboScore += points;
		
		int finalScore = (int) (comboScore * multiplier);
		scene.addScore(finalScore);
		
		// Handle achievements
		if (multiplier > 3) 
			trickUI.addAchievement("\n#gBIG LINK!");
		
		if (hangtime > 1f)
			trickUI.addAchievement("\n#gBIG AIR!");
		
		if (player.getGrindLen() > MIN_GRIND_LEN) {
			float len = (float)(player.getGrindLen()) / 11f;
			
			trickUI.addAchievement("\n#gGRIND LENGTH: " + String.format("%.2f", len) + "m");
		}
		
		trickUI.setTrickSubstring("" + finalScore);
		
		currentTrick = null;
		comboScore = 0;
		multiplier = .5f;
	}

	private int getPoints(Trick trick) {
		int points = trick.getPoints();
		
		if (trickDegregation.containsKey(trick))
			points /= trickDegregation.get(trick);

		if (points < 10)
			points = 0;
		
		return points;
	}

	private void startTrick() {
		
		if (currentTrick != null && currentTrick.isLandBackwards()) {
			Joint joint = player.getModel().getSkeleton().getJoint("board");
			player.getAnimator().getJointTransform(joint.index).getPosition().add(19, 100, 100);
		}
		
		trickUI.setTimer(3f);
		

		hangtime = 0f;
		
		comboScore += getPoints(bufferedTrick);
		multiplier += .5f;
		degradeTrick(bufferedTrick);
		
		if (player.isGrounded() && !player.isGrinding())
			trickUI.clearTrickString();
		
		trickUI.addToTrickString(bufferedTrick, getPoints(bufferedTrick), comboScore, multiplier);
		
		if (bufferedTrick.getType() == TrickType.KICK_TRICK)
			player.jump();
		
		Animation anim = bufferedTrick.getAnimation();
		player.getAnimator().start(anim);
		
		currentTrick = bufferedTrick;
		trickTimer = anim.getDuration();
		bufferedTrick = null;
	}

	

	public boolean getPlayerIsBuffering() {
		return isBuffering;
	}
}
