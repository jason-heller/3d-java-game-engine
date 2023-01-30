package scene.mapscene.trick;

import java.util.HashMap;
import java.util.Map;

import gl.Window;
import gl.anim.Animation;
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
	
	private Map<Integer, Integer> trickDegregation;
	private float currentDeg = 0f;
	
	private TrickUI trickUI;
	
	private static final float TRICK_STRING_MULTIPLIER = .25f;
	
	private float comboScore = 0;
	private float multiplier = 1f - TRICK_STRING_MULTIPLIER;
	
	private static final float MIN_GRIND_LEN = 10f * 11f;
	private static final float MAX_GRIND_POINTS_PER_SECOND = 50;
	private static final float MAX_GRIND_POINTS_PENALTY = 40;
	
	private boolean hasFlippedStance = true;
	
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
		
		if (currentTrick == null && player.isGrounded() && !player.isGrinding()) {
			comboScore = 0;
			multiplier = 1f - TRICK_STRING_MULTIPLIER;
		}

		if (grinding) {
			trickUI.updateTrickSubtring((int)comboScore, multiplier);
			comboScore += Window.deltaTime * (MAX_GRIND_POINTS_PER_SECOND - Math.min(currentDeg * 5f, MAX_GRIND_POINTS_PENALTY));
			trickUI.setTimer(3f);
		} else {
			grindCooldownTimer = Math.max(grindCooldownTimer - Window.deltaTime, 0f);
		}
		
		trickUI.update();
		
		if (trickTimer > 0f) {
			trickTimer = Math.max(trickTimer - Window.deltaTime, 0f);
		}

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
				if (TR_OLLIE)
					trickFlags &= (Integer.MAX_VALUE - 12);
				
				if (grounded) {
					newTrick = TrickList.getTrick(TrickType.KICK_TRICK, trickFlags);
				}
			}
			
			if (TR_GRIND && !grinding && grindCooldownTimer == 0f) {
				Trick trick = TrickList.getTrick(TrickType.GRIND_TRICK, trickFlags);
				
				if (!grinding && trick != null) {
					player.startGrind();
					
					// Reset the trick string if not comboing
					if (player.isGrounded())
						trickUI.clearTrickString();
					
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
		int trickId = player.isSwitch() ? -trick.id : trick.id;
		Integer value = trickDegregation.get(trickId);
		
		if (value == null) {
			trickDegregation.put(trickId, 1);
			currentDeg = 1;
		} else {
			int multiplier = value.intValue() + 1;
			trickDegregation.put(trickId, multiplier);
			currentDeg = multiplier;
		}
	}

	public void handleOnComboEnd() {
		if (currentTrick == null)
			return;

		float bonus;
		
		// Handle achievements
		if (multiplier > 3) 
			trickUI.addAchievement("\n#gBIG LINK!");
		
		if (hangtime > 1f) {
			trickUI.addAchievement("\n#gBIG AIR!");
		
			bonus = 1f + (hangtime / 1.5f);
			multiplier *= bonus;
			trickUI.addAchievement("\n  #b+" + String.format("%.1f", bonus) + "x bonus");
		}
		
		if (player.getGrindLen() > MIN_GRIND_LEN) {
			float len = (float)(player.getGrindLen()) / 11f;
			
			trickUI.addAchievement("\n#gGRIND LENGTH: " + String.format("%.1f", len) + "m");

			if (len >= 12f) {
				bonus = ((len - 12f) / 8f) + 1.1f;
				multiplier *= bonus;
				trickUI.addAchievement("\n  #b+" + String.format("%.1f", bonus) + "x bonus");
			}
		}
		
		int finalScore = (int) (comboScore * multiplier);
		scene.addScore(finalScore);
		
		trickUI.setTrickSubstring("" + finalScore);
		
		currentTrick = null;
		comboScore = 0f;
		multiplier = 1f - TRICK_STRING_MULTIPLIER;
	}

	private int getPoints(Trick trick) {
		int points = trick.getPoints();
		int trickId = player.isSwitch() ? -trick.id : trick.id;
		
		if (trickDegregation.containsKey(trickId))
			points /= trickDegregation.get(trickId);

		if (points < 10)
			points = 0;
		
		return points;
	}

	private void startTrick() {
		if (!hasFlippedStance)
			flipStance();
		
		trickUI.setTimer(3f);
		
		// Reset the trick string if not comboing
		if (!player.isGrinding())
			trickUI.clearTrickString();

		hangtime = 0f;
		
		comboScore += getPoints(bufferedTrick);
		multiplier += TRICK_STRING_MULTIPLIER;
		degradeTrick(bufferedTrick);
		
		trickUI.addToTrickString(player, bufferedTrick, getPoints(bufferedTrick), (int)comboScore, multiplier);
		
		if (bufferedTrick.getType() == TrickType.KICK_TRICK)
			player.jump();
		
		if (bufferedTrick.isLandSwitch())
			hasFlippedStance = false;
		
		Animation anim = bufferedTrick.getAnimation();
		
		player.trickEndFlagHandler();
		player.getAnimator().start(anim);
		
		currentTrick = bufferedTrick;
		trickTimer = anim.getDuration();
		bufferedTrick = null;
	}

	public boolean getPlayerIsBuffering() {
		return isBuffering;
	}

	public Trick getCurrentTrick() {
		return currentTrick;
	}

	public boolean hasFlippedStance() {
		return this.hasFlippedStance;
	}

	public void flipStance() {
		player.setSwitch(!player.isSwitch());
		hasFlippedStance = true;
	}
}
