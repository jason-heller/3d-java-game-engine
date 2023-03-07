package scene.entity.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import core.Resources;
import dev.cmd.Console;
import geom.Plane;
import gl.Camera;
import gl.CameraFollowable;
import gl.Window;
import gl.anim.Animator;
import gl.anim.Ragdoll;
import gl.anim.component.Joint;
import gl.anim.component.Skeleton;
import io.Input;
import map.architecture.util.BspRaycast;
import scene.PlayableScene;
import scene.mapscene.MapScene;
import scene.mapscene.trick.Trick;
import scene.mapscene.trick.TrickManager;
import ui.UI;
import util.Colors;
import util.MathUtil;
import util.Vectors;

/**
 * @author Jason
 *
 */
public class PlayerEntity extends SkatePhysicsEntity implements CameraFollowable {
	
	////////////////////////////////
	
	public static final float CAMERA_STANDING_HEIGHT = 5f;

	public static final float BBOX_WIDTH = 1.5f, BBOX_HEIGHT = 6f;
	
	public static boolean viewGrindState = false;
	public static boolean REGULAR_STANCE = true, GOOFY_STANCE = false;
	
	////////////////////////////////
	
	private Camera camera;
	
	private static int hp = 15;
	private static int maxHp = 15;
	
	private float turnSpeed = 0f;
	private float acceleration;
	private float pushTimer = 0f;
	
	public static boolean enabled = false;
	
	private TrickManager trickManager;
	
	//private boolean boardIsFacingBack = false;
	private final boolean stance = REGULAR_STANCE;
	private boolean ridingSwitch = false;

	private Ragdoll ragdoll;
	
	public PlayerEntity(MapScene scene) {
		super("player", new Vector3f(BBOX_WIDTH, BBOX_HEIGHT, BBOX_WIDTH));
		this.camera = scene.getCamera();
		this.setModel("untitled");
		this.setAnimator(new Animator(getModel().getSkeleton(), this));
		getAnimator().start("idle");
		
		trickManager = new TrickManager(scene, this);
	}
	
	@Override
	public void update(PlayableScene scene) {
		if (!enabled)
			return;
		
		boolean isNotBuffering = !trickManager.getPlayerIsBuffering();

		boolean LEFT = Input.isDown("left"),
				RIGHT = Input.isDown("right"),
				DOWN = Input.isDown("down"),
				UP = Input.isDown("up");
		
		final boolean LEFT_RELEASED = Input.isReleased("left"),
				RIGHT_RELEASED = Input.isReleased("right");
		
		if ((Console.isVisible() && Console.isBlocking)) {
			LEFT = RIGHT = false;
			DOWN = true;
		}
		
		/*if (Input.isPressed(Keyboard.KEY_K)) {
			if (ragdoll == null) {
				ragdoll = new Ragdoll(boardPos, localVelocity, getModel().getSkeleton(), this);
			} else {
				ragdoll = null;
			}
			
			getAnimator().setRagdoll(ragdoll);
		}*/
		
		if (DOWN && isNotBuffering && grounded && !isComboing())
			acceleration = Math.max(acceleration - Window.deltaTime * 400f, 0f);

		// Handle game logic per tick, such as movement etc
		float targetTurnSpeed = 0f;
	
		if (grindRail == null) {
			if (!previouslyGrounded && grounded && baseVelocity.y <= 0f) {
				trickEndFlagHandler();
				animator.start("land");
				rideSfxSource.play("ride");
				trickManager.handleOnComboEnd();
				grindLen = 0f;
			}
			
			if (grounded) {
				
				if (isNotBuffering) {
					if (LEFT && RIGHT) {
						if (pushTimer == 0f)
							animator.start("idle");
					} else if (LEFT) {
						targetTurnSpeed = -3f;
						if (pushTimer == 0f)
							animator.start(ridingSwitch ? "turn_l" : "turn_r");
					} else if (RIGHT) {
						targetTurnSpeed = 3f;
						if (pushTimer == 0f)
							animator.start(ridingSwitch ? "turn_r" : "turn_l");
					}
					
					if ((LEFT_RELEASED || RIGHT_RELEASED) && pushTimer == 0f) {
						animator.start("idle");
					}
				}
				
				if (UP && pushTimer == 0f) {
					this.animator.start("push");
					pushTimer = Window.deltaTime;
				}
				
				if (pushTimer != 0f) {
					
					pushTimer += Window.deltaTime;
				
					if (pushTimer > Resources.getAnimation("push").getDuration()) {
						acceleration += 50;
						pushTimer = 0f;
					} 
				}
				
				if (targetTurnSpeed == 0f) {
					turnSpeed = Math.max(Math.abs(turnSpeed) - Window.deltaTime * 30f, 0f) * Math.signum(turnSpeed);
				} else {
					if (Math.signum(targetTurnSpeed) != Math.signum(turnSpeed))
						turnSpeed = 0f;
					
					turnSpeed += Window.deltaTime * ((targetTurnSpeed > turnSpeed) ? 12f : -12f);
				}
				
				direction += Window.deltaTime * turnSpeed;
			}
		}
		
		if (ragdoll != null)
			ragdoll.update(arc.bsp);

		float accelSlope = Window.deltaTime * accelSlopeFactor;
		if (bbox.Z.y > .3f) {
			acceleration -= accelSlope;
			
			if (acceleration <= 0f) {
				direction += MathUtil.PI;
				acceleration = 0f;
				// this.animator.start("kickturn_r");
			}
		} else if (bbox.Z.y < -.3f) {
			acceleration += accelSlope;
		} else {
			float factor = (acceleration > baseAccelSpeed) ? -decelSlopeFactor : accelSlopeFactor;
			float deltaAccel = Window.deltaTime * factor;
			// acceleration = Math.max(Math.abs(acceleration) + deltaAccel, baseAccelSpeed) * Math.signum(acceleration);
			acceleration += deltaAccel;
		}
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			if (camera.getFocus() == this) {
				camera.getPosition().set(position.x, position.y + Camera.offsetY, position.z);
			}
		}
		
		if (!deactivated && grindRail == null) {
			Camera.animSpeed = 4.5f;
			
			float accel = acceleration;
			
			if (!grounded) {
				Camera.animSpeed = 0f;
				accel = airAccel;
			}
			
			accelerate(bbox.Z, accel);
		}

		
		if (vertAxis == null) {
			viewAngle.x = -direction;
			viewAngle.y = ((bbox.Z.y > 0) ? .5f : 1f) * -bbox.Z.y * MathUtil.HALFPI;
		} else {
			viewAngle.y = 1.2f;//MathUtil.HALFPI - .45f;
			
		}
		
		scale.x = stance ^ ridingSwitch ? -1f : 1f;

		Camera.animSpeed = 0f;
		super.update(scene);

		trickManager.update();

		if (!trickManager.hasFlippedStance() && animator.getCurrentAnimation().equals("fall")) {
			trickManager.flipStance();
		}
	}
	
	@Override
	protected void handleGrindState() {
		
		super.handleGrindState();

		if (viewGrindState) {
			UI.drawRect(560, 195, 160, 10, Colors.BLACK);
			UI.drawRect(638 + (this.grindBalance)*2, 190, 4, 20, Colors.CYAN);
		}
	}

	@Override
	public void endGrind() {
		super.endGrind();
		//((ThirdPersonCameraController) camera.getFocus()).setTrackingSpeed(30f);
	}

	public void heal(int health) {
		hp += health;
	}
	
	@Override
	protected void collideWithFloor(Plane plane) {
		/*float fallHeight = -PlayerHandler.jumpVel * 3.4f;
		if (vel.y < fallHeight) {
			takeDamage((int) (-vel.y / 20f));
			AudioHandler.play("fall");
			vel.y = -PlayerHandler.jumpVel;	// TODO: Bad
		}*/
		
		super.collideWithFloor(plane);
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

	@Override
	public Vector3f getViewAngle() {
		return viewAngle;
	}

	@Override
	public Vector3f getPosition() {
		return position;
	}

	public Quaternionf getRotation() {
		return rotation;
	}

	public void startGrind() {
		if (this.grindRail != null) {
			this.endGrind();
		} else {
			grind();
			//((ThirdPersonCameraController) camera.getFocus()).setTrackingSpeed(CAM_GRIND_FOLLOW_SPEED);
		}
	}

	public boolean isGrinding() {
		return grindRail != null;
	}

	public void jump() {
		// Jump off rail
		if (grindRail != null) {
			float exitDir = (float)Math.toRadians((Math.abs(grindBalance) < 20) ? 0f : this.grindBalance / 1.8f);
			direction += exitDir;
	
			localVelocity.rotateY(-exitDir);
		}
		
		jump(jumpVel);
		endGrind();
		rideSfxSource.stop();
		trickSfxSource.play("ollie");
	}
	
	public void trickEndFlagHandler() {
		Trick currentTrick = trickManager.getCurrentTrick();
		
		if (currentTrick == null)
			return;
		
		if (currentTrick.isLandBackwards()
				&& getAnimator().getCurrentAnimation().equals(currentTrick.getAnimation().getName())) {
			Skeleton skeleton = model.getSkeleton();
			Joint joint = skeleton.getJoint("board");

			getAnimator().getCurrentJointTransforms()[joint.index].getRotation().rotateZ(3.14159f);
		}
	}

	public void setSwitch(boolean isSwitch) {
		this.ridingSwitch = isSwitch;
	}

	public boolean isSwitch() {
		return ridingSwitch;
	}

	public boolean getFrontside() {
		float dx = bbox.X.x;
		float dz = bbox.X.z;

		Vector3f orig = new Vector3f((position.x - dx * .25f), position.y, (position.z - dz * .25f));
		BspRaycast ray = arc.raycast(orig, Vectors.NEGATIVE_Y);
	
		return (ray == null || ray.getDistance() >= bbox.getHeight() + 1) ^ ridingSwitch ^ stance;
	}

	public Trick getCurrentTrick() {
		return trickManager.getCurrentTrick();
	}
	
	public boolean isRegularStance() {
		return stance;
	}

	public boolean isInVert() {
		return this.vertAxis != null;
	}

	public TrickManager getTrickManager() {
		return trickManager;
	}

	public boolean isComboing() {
		return !trickManager.getComboList().isEmpty();
	}
}
