package scene.mapscene.trick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.cmd.Console;
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
	private float trickCooldownTimer = 0f;
	private int lastControlBuffer = -1;
	private float trickDelay = 0f;
	
	private float hangtime = 0f;
	
	private Map<Integer, Integer> trickDegregation;
	private float currentDeg = 0f;
	
	private TrickUI trickUI;
	
	private List<Trick> comboList;
	
	private static final float TRICK_STRING_MULTIPLIER = .25f;
	
	private float comboScore = 0;
	private float multiplier = 1f - TRICK_STRING_MULTIPLIER;
	
	private static final float MIN_GRIND_LEN = 10f * 11f;
	private static final float MAX_GRIND_POINTS_PER_SECOND = 50;
	private static final float MAX_GRIND_POINTS_PENALTY = 40;
	
	private boolean hasFlippedStance = true;
	private int highestCombo;
	
	public TrickManager(MapScene scene, PlayerEntity player) {
		this.scene = scene;
		this.player = player;
		trickDegregation = new HashMap<>();
		
		trickUI = new TrickUI();
		comboList = new ArrayList<>();
	}

	public void update() {
		final boolean grounded = player.isGrounded();
		final boolean grinding = player.isGrinding();
		
		final boolean TR_OLLIE = Input.isDown("tr_ollie"),
				TR_FLIP = Input.isDown("tr_flip"),
				TR_AIR = Input.isDown("tr_air"),
				TR_MODIFIER_LEFT = Input.isDown("tr_modifier_l"),
				TR_MODIFIER_RIGHT = Input.isDown("tr_modifier_r"),
				TR_GRIND = Input.isDown("tr_grind");
		
		if (!grounded) {
			hangtime += Window.deltaTime;
			
			if (currentTrick != null) {
				if (currentTrick.getType() == TrickType.GRAB_TRICK && !player.getAnimator().getCurrentAnimation().equals("fall")) {
					trickUI.updateTrickSubtring((int)comboScore, multiplier);
					comboScore += hangtime * 2f;
					trickUI.setTimer(3f);
					
					if (!TR_AIR && this.trickTimer == 0f)
						player.getAnimator().start("fall");
					
				} else if (currentTrick.getType() == TrickType.KICK_TRICK) {
					trickUI.setTimer(3f);
				}
			}
		}
		
		if (player.isInVert() && bufferedTrick != null) {
			startTrick();
		}
		
		if (currentTrick == null && player.isGrounded() && !player.isGrinding()) {
			comboScore = 0;
			multiplier = 1f - TRICK_STRING_MULTIPLIER;
			comboList.clear();
		}

		if (grinding) {
			trickUI.updateTrickSubtring((int)comboScore, multiplier);
			comboScore += Window.deltaTime * (MAX_GRIND_POINTS_PER_SECOND - Math.min(currentDeg * 5f, MAX_GRIND_POINTS_PENALTY));
			trickUI.setTimer(3f);
		} else {
			trickCooldownTimer = Math.max(trickCooldownTimer - Window.deltaTime, 0f);
		}
		
		trickUI.update();
		
		if (trickTimer > 0f) {
			trickTimer = Math.max(trickTimer - Window.deltaTime, 0f);
		}
		
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
			
			if (!isBuffering && !grinding && grounded && trickFlags >= 16) {
				player.getAnimator().start("charge");
			}
			
			if (TR_FLIP || TR_OLLIE) {
				if (TR_OLLIE)
					trickFlags &= (Integer.MAX_VALUE - 12);
				
				if (grounded) {
					newTrick = TrickList.getTrick(TrickType.KICK_TRICK, trickFlags);
				}
			}
			
			if (TR_AIR && !grounded) {
				if (currentTrick == null || player.getAnimator().getCurrentAnimation().equals("fall")) {
					trickDelay += Window.deltaTime;
					
					if (trickDelay > .13) {
						bufferedTrick = TrickList.getTrick(TrickType.GRAB_TRICK, trickFlags);
						trickDelay = 0f;
						
						if (bufferedTrick != null)
							startTrick();
					}
					
				}
			}
			
			if (TR_GRIND && !grinding && trickCooldownTimer == 0f) {
				Trick trick = TrickList.getTrick(TrickType.GRIND_TRICK, trickFlags);
				
				if (!grinding && trick != null) {
					player.startGrind();
					
					// Reset the trick string if not comboing
					if (player.isGrounded())
						endCombo();
					
					if (player.isGrinding()) {
						bufferedTrick = trick;
						startTrick();
						trickCooldownTimer = .2f;
					}
					
				}
			}
			
			isBuffering = grounded && (TR_FLIP || TR_OLLIE);
			
			if (lastBuffered != newTrick) {
				if (trickFlags < lastControlBuffer) {
					holdBufferTimer = .065f;
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
		
		if (hangtime > 2f) {
			trickUI.addAchievement("\n#gBIG AIR!");
		
			bonus = 1f + (hangtime / 1.5f);
			multiplier *= bonus;
			trickUI.addAchievement("\n  #b+" + String.format("%.1f", bonus) + "x bonus");
		}
		
		if (player.getGrindLen() > MIN_GRIND_LEN) {
			float len = (float)(player.getGrindLen()) / 12f;
			
			trickUI.addAchievement("\n#gGRIND LENGTH: " + String.format("%.1f", len) + "m");

			if (len >= 12f) {
				bonus = ((len - 12f) / 8f) + 1.1f;
				multiplier *= bonus;
				trickUI.addAchievement("\n  #b+" + String.format("%.1f", bonus) + "x bonus");
			}
		}
		
		int finalScore = (int) (comboScore * multiplier);
		
		if (!MapScene.preRound) {
			scene.addScore(finalScore);
			highestCombo = (int) Math.max(highestCombo, comboScore * multiplier);
		}
		
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
		if (!player.isGrinding() && !player.getAnimator().getCurrentAnimation().equals("fall"))
			endCombo();

		hangtime = 0f;
		
		comboScore += getPoints(bufferedTrick);
		multiplier += TRICK_STRING_MULTIPLIER;
		
		if (!MapScene.preRound)
			degradeTrick(bufferedTrick);
		
		trickUI.addToTrickString(player, bufferedTrick, getPoints(bufferedTrick), (int)comboScore, multiplier);
		
		if (bufferedTrick.getType() == TrickType.KICK_TRICK && player.isGrounded())
			player.jump();
		
		if (bufferedTrick.isLandSwitch())
			hasFlippedStance = false;
		
		Animation anim = bufferedTrick.getAnimation();
		
		player.trickEndFlagHandler();
		player.getAnimator().start(anim);
		
		currentTrick = bufferedTrick;
		trickTimer = anim.getDuration();
		bufferedTrick = null;
		
		comboList.add(currentTrick);
	}

	private void endCombo() {
		trickUI.clearTrickString();
		comboList.clear();
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

	public int getHighestCombo() {
		return highestCombo;
	}

	public List<Trick> getComboList() {
		return comboList;
	}

	public void addTrick(String trickName, int points, float addedMultiplier) {
		trickUI.addToTrickString(trickName, points, (int)comboScore, addedMultiplier);
		
		multiplier += addedMultiplier;
		comboScore += points;
	}

	public boolean comboContains(TrickType targetType) {
		for(int i = 0; i < comboList.size(); i++) {
			if (comboList.get(i).getType() == targetType)
				return true;
		}
		
		return false;
	}
	
	public boolean comboContains(Trick targetTrick) {
		for(int i = 0; i < comboList.size(); i++) {
			if (comboList.get(i) == targetTrick)
				return true;
		}
		
		return false;
	}
	
	public boolean comboContainsGap(String gap) {
		return this.trickUI.getTrickString().contains("#b" + gap);
	}

	public boolean comboContains(Trick[] requiredTricks) {
		boolean[] fulfilled = new boolean[requiredTricks.length];
		
		for(int i = 0; i < requiredTricks.length; i++) {
			Trick trick = requiredTricks[i];
			for(int j = 0; j < comboList.size(); j++) {
				if (comboList.get(j) == trick) {
					fulfilled[j] = true;
					break;
				}
			}
		}
		
		for(int i = 0; i < fulfilled.length; i++) {
			if (!fulfilled[i])
				return false;
		}
		
		return true;
	}
	
	public boolean comboContainsOrdered(Trick[] requiredTricks) {
		int fulfilled = 0;
		
		for(int i = 0; i < comboList.size(); i++) {
			if (comboList.get(i) == requiredTricks[0]) {
				for(int j = 1; j < requiredTricks.length && i + j < comboList.size(); j++) {
					if (comboList.get(i + j) != requiredTricks[j])
						return false;
					
					fulfilled++;
				}
				
				return fulfilled == requiredTricks.length - 1;
			}
		}
		
		return false;
	}
}
