package scene.entity.util;

import org.joml.Vector3f;

import geom.Plane;
import gl.Camera;
import gl.CameraFollowable;
import gl.Window;
import gl.anim.Animator;
import io.Input;
import scene.PlayableScene;
import scene.mapscene.MapScene;
import scene.mapscene.trick.TrickManager;
import ui.UI;
import util.Colors;
import util.MathUtil;

/**
 * @author Jason
 *
 */
public class PlayerEntity extends SkatePhysicsEntity implements CameraFollowable {
	
	////////////////////////////////
	
	public static final float CAMERA_STANDING_HEIGHT = 5f;
	
	private static final float CAM_FOLLOW_SPEED = 30f, CAM_GRIND_FOLLOW_SPEED = 5f;

	public static final float BBOX_WIDTH = 1.5f, BBOX_HEIGHT = 6f;

	private static final float RAILTRAJECT_ADJSPEED = 50f;
	private static final float RAILTRAJECT_MAX = 25f, RAILTRAJECT_MIN = 8f;
	
	public static boolean viewGrindState = false;
	
	////////////////////////////////
	
	private Camera camera;
	
	private static int hp = 15;
	private static int maxHp = 15;
	
	public static boolean isEnabled = false;
	
	private Vector3f viewAngle = new Vector3f();
	
	private float railExitTrajectory = 0f;
	
	private TrickManager trickManager;
	
	//private boolean boardIsFacingBack = false;
	private boolean ridingSwitch = false;
	
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
		
		if (!isEnabled)
			return;
		
		if (camera.getControlStyle() == Camera.SPECTATOR) {
			return;
		}
		
		boolean isNotBuffering = !trickManager.getPlayerIsBuffering();

		final boolean LEFT = Input.isDown("left"),
				RIGHT = Input.isDown("right"),
				DOWN = Input.isDown("down");
		
		final boolean LEFT_RELEASED = Input.isReleased("left"),
				RIGHT_RELEASED = Input.isReleased("right");
		
		// Architecture arc = ((PlayableScene)scene).getArchitecture();
		
		float speed = accelSpeed;
		
		
		trickManager.update();
		
		if (DOWN && isNotBuffering)
			speed = 0f;

		// Handle game logic per tick, such as movement etc
		float turnSpeed = isNotBuffering ? 240 : 100;
	
		if (grindRail == null) {
			if (!previouslyGrounded && grounded && vel.y <= 0f) {
				getAnimator().start("land");
				rideSfxSource.play("ride");
				trickManager.handleOnTrickEnd();
				grindLen = 0f;
			}
			
			if (grounded) {
				
				if (this.vel.lengthSquared() < 1600) {
					turnSpeed /= 2f;
				}
				
				if (LEFT && RIGHT && isNotBuffering) {
					getAnimator().start("idle");
				} else if (LEFT) {
					direction -= Window.deltaTime * turnSpeed;
					
					if (isNotBuffering)
						getAnimator().start("turn_l");
				} else if (RIGHT) {
					direction += Window.deltaTime * turnSpeed;
					
					if (isNotBuffering)
						getAnimator().start("turn_r");
				}
				
				if ((LEFT_RELEASED || RIGHT_RELEASED) && isNotBuffering) {
					getAnimator().start("idle");
				}
			}
		} else {
			rot.y = (float) Math.toDegrees(MathUtil.pointDirection(0, 0, grindNormal.x, grindNormal.z)) + 90f;
			direction = -rot.y;
		}
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			if (camera.getFocus() == this) {
				camera.getPosition().set(pos.x, pos.y + Camera.offsetY, pos.z);
			}
		}
		
		if (!deactivated && grindRail == null) {
			Camera.animSpeed = 4.5f;
			if (!grounded) {
				Camera.animSpeed = 0f;
				speed = airAccel;
			}
			
			rot.y = -direction;
			double dRad = direction * Math.PI / 180.0;
			accelerate(new Vector3f(-(float) Math.sin(dRad), 0, (float) Math.cos(dRad)), speed);
		}
		
		viewAngle.set(0f, rot.y, 0f);
		
		Camera.animSpeed = 0f;
		super.update(scene);
	
		if (ridingSwitch) {
			/*Joint joint = model.getSkeleton().getJoint("pubis");
			for(int i = joint.index; i < model.getSkeleton().getNumJoints(); i++) {
				getAnimator().getJointTransforms()[i].rotateY(180f);
			}*/
		}
	}
	
	@Override
	protected void handleGrindState() {
		final boolean LEFT = Input.isDown("left"),
				RIGHT = Input.isDown("right");
		
		super.handleGrindState();
		
		if (LEFT) {
			railExitTrajectory = Math.min(railExitTrajectory - Window.deltaTime * RAILTRAJECT_ADJSPEED, -RAILTRAJECT_MIN);
		} else if (RIGHT) {
			railExitTrajectory = Math.max(railExitTrajectory + Window.deltaTime * RAILTRAJECT_ADJSPEED, RAILTRAJECT_MIN);
		} else {
			railExitTrajectory = 0f;
		}
		
		railExitTrajectory = MathUtil.clamp(railExitTrajectory, -RAILTRAJECT_MAX, RAILTRAJECT_MAX);
		
		if (viewGrindState) {
			UI.drawRect(560, 195, 160, 10, Colors.BLACK);
			if (railExitTrajectory > 0f) {
				UI.drawRect(640, 193, ((railExitTrajectory / RAILTRAJECT_MAX) * 80), 2, Colors.PURPLE);
			} else {
				float depth = 640 + ((railExitTrajectory / RAILTRAJECT_MAX) * 80);
				UI.drawRect(depth, 193, 640 - depth, 2, Colors.PURPLE);
			}
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
		return pos;
	}

	public Vector3f getRotation() {
		return rot;
	}

	public void startGrind() {
		if (this.grindRail != null) {
			this.endGrind();
		} else {
			grind();
			railExitTrajectory = 0f;
			//((ThirdPersonCameraController) camera.getFocus()).setTrackingSpeed(CAM_GRIND_FOLLOW_SPEED);
		}
	}

	public boolean isGrinding() {
		return grindRail != null;
	}

	public void jump() {
		// Jump off rail
		if (grindRail != null) {
			float exitDir = railExitTrajectory;
			direction += exitDir;
			float turnRad = (float) Math.toRadians(-exitDir);
	
			vel.rotateY(turnRad);
		}
		
		jump(jumpVel);
		endGrind();
		rideSfxSource.stop();
		trickSfxSource.play("ollie");
	}

	public void setSwitch(boolean isSwitch) {
		this.ridingSwitch = isSwitch;
	}

	public boolean isSwitch() {
		return ridingSwitch;
	}
}
