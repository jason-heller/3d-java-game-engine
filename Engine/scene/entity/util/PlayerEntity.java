package scene.entity.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.cmd.Console;
import geom.Plane;
import gl.Camera;
import gl.CameraFollowable;
import gl.Window;
import gl.anim.Animator;
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
	
	public static boolean isEnabled = false;
	
	private Vector3f viewAngle = new Vector3f();
	
	private TrickManager trickManager;
	
	//private boolean boardIsFacingBack = false;
	private final boolean stance = REGULAR_STANCE;
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
		if (getAnimator().getCurrentAnimation().equals("fall")) {
			this.setColor(Vectors.POSITIVE_X);
		}
		
		rotation.identity();
		
		if (!isEnabled)
			return;
		
		boolean isNotBuffering = !trickManager.getPlayerIsBuffering();

		boolean LEFT = Input.isDown("left"),
				RIGHT = Input.isDown("right"),
				DOWN = Input.isDown("down");
		
		final boolean LEFT_RELEASED = Input.isReleased("left"),
				RIGHT_RELEASED = Input.isReleased("right");
		
		if (camera.getControlStyle() == Camera.SPECTATOR || Console.isVisible()) {
			LEFT = RIGHT = false;
			DOWN = true;
		}
		
		// Architecture arc = ((PlayableScene)scene).getArchitecture();
		
		float speed = accelSpeed;

		if (DOWN && isNotBuffering)
			speed = 0f;

		// Handle game logic per tick, such as movement etc
		float turnSpeed = isNotBuffering ? 4.1f : 0;
	
		if (grindRail == null) {
			if (!previouslyGrounded && grounded && vel.y <= 0f) {
				trickEndFlagHandler();
				getAnimator().start("land");
				rideSfxSource.play("ride");
				trickManager.handleOnComboEnd();
				grindLen = 0f;
			}
			
			if (grounded) {
				
				if (this.vel.lengthSquared() < 1000) {
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
			float newRotation = MathUtil.pointDirection(0, 0, grindNormal.x, grindNormal.z) + MathUtil.HALFPI;
			rotation.rotateY(newRotation);
			direction = -newRotation;
		}
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			if (camera.getFocus() == this) {
				camera.getPosition().set(position.x, position.y + Camera.offsetY, position.z);
			}
		}
		
		if (!deactivated && grindRail == null) {
			Camera.animSpeed = 4.5f;
			if (!grounded) {
				Camera.animSpeed = 0f;
				speed = airAccel;
			}
			
			rotation.setAngleAxis(-direction, 0, 1, 0);
			accelerate(new Vector3f(-(float) Math.sin(direction), 0, (float) Math.cos(direction)), speed);
		}
		
		viewAngle.set(0f, -direction, 0f);
		
		scale.x = stance ^ ridingSwitch ? -1f : 1f;

		if (ridingSwitch) {
			UI.drawString("(Switch)", 80, 540);
		}
		
		Camera.animSpeed = 0f;
		super.update(scene);

		trickManager.update();
		// Trick currentTrick = getCurrentTrick();

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
			float exitDir = (float)Math.toRadians((Math.abs(grindBalance) < 20) ? 0f : this.grindBalance / 2f);
			direction += exitDir;
	
			vel.rotateY(-exitDir);
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
		Vector3f euler = new Vector3f();
		rotation.getEulerAnglesXYZ(euler);
		float dirRad = euler.y;

		float dx = (float) -Math.cos(dirRad);
		float dz = (float) Math.sin(dirRad);

		Vector3f orig = new Vector3f((position.x + dx) + (dz * 2f), position.y, (position.z + dz) - (dx * 2f));
		BspRaycast ray = arc.raycast(orig, new Vector3f(0, -1, 0));

		return (ray == null || ray.getDistance() >= bbox.getHeight() + 1) ^ ridingSwitch ^ stance;
	}

	public Trick getCurrentTrick() {
		return trickManager.getCurrentTrick();
	}
	
	public boolean isRegularStance() {
		return stance;
	}
}
