package scene.entity.utility;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import gl.Camera;
import gl.Window;
import gl.post.PostProcessing;
import io.Input;
import scene.Scene;
import util.MathUtil;

public class PlayerHandler {
	private static PlayerEntity entity;
	
	public static final float WALKER_SPEED_MULTIPLIER = 1.25f;
	public static float speedPenaltyMultiplier = 1f;
	public static boolean hasWalker = true;

	public static float jumpVel = 20f;
	public static float friction = 10f, airFriction = 1f;
	public static float maxSpeed = 100f, maxAirSpeed = 100f, maxWaterSpeed = 32f;
	public static float accelSpeed = 100f, airAccel = 25f, waterAccel = 32f;
	
	private static final float CAMERA_STANDING_HEIGHT = 3f;
	private static final float CAMERA_CROUCHING_HEIGHT = 2f;
	private static final float CROUCH_SPEED = 4f;
	private static final float MOVE_SPEED = 75f;
	
	private static float walkSfxTimer = 0f;

	private static boolean disabled = false;
	
	public static PhysicsEntity getEntity() {
		return entity;
	}

	private static void passPhysVars() {
		final float speedScale = speedPenaltyMultiplier * (hasWalker ? WALKER_SPEED_MULTIPLIER : 1f);
		accelSpeed = MOVE_SPEED * speedScale;
		entity.maxSpeed = maxSpeed;
		entity.maxAirSpeed = maxAirSpeed;
		entity.maxWaterSpeed = maxWaterSpeed;
		entity.friction = friction;
		entity.airFriction = airFriction;
	}

	public static void setEntity(PlayerEntity entity) {
		Camera.offsetY = CAMERA_STANDING_HEIGHT;
		PlayerHandler.entity = entity;
		enable();
	}

	public static void update(Scene scene) {
		passPhysVars();
		if (disabled) {
			return;
		}
		
		final Camera camera = scene.getCamera();
		
		float speed = 0;
		final float yaw = camera.getYaw();
		float direction = yaw;

		final boolean A = Input.isDown("walk_left"), D = Input.isDown("walk_right"), W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"), JUMP = Input.isDown("jump"), RUN = Input.isDown("run");
		final boolean CTRL = Input.isDown("sneak");
		
		PostProcessing.underwater = entity.isFullySubmerged();

		if (entity.isSubmerged()) {
			waterPhysics(scene);
		} else if (entity.isClimbing()) {
			climbingPhysics(scene);
		} else {
			// Handle game logic per tick, such as movement etc
			if (A && D) {
			} else if (A) {
				direction = yaw + 90;
				speed = accelSpeed;
			} else if (D) {
				direction = yaw - 90;
				speed = accelSpeed;
			}
	
			if (W && S) {
			} else if (S && !getEntity().isSliding()) {
				if (direction != yaw) {
					direction += 45 * (direction > yaw ? -1f : 1f);
				}
	
				speed = accelSpeed;
			} else if (W && !getEntity().isSliding()) {
	
				if (direction != yaw) {
					direction -= 45 * (direction > yaw ? -1f : 1f);
				} else {
					direction = yaw + 180;
				}
	
				
				float modifier = 1f;
				if (RUN && !CTRL) {
					modifier = 2.5f;
					camera.sway(.1f, 5f, 5f);
				}
				
				speed = accelSpeed * modifier;
			}
	
			if ((getEntity().isGrounded() || getEntity().isSubmerged() && getEntity().vel.y < 0) && JUMP) {
				getEntity().jump(jumpVel);
				if (CTRL) {
					getEntity().pos.y += Camera.offsetY - CAMERA_CROUCHING_HEIGHT;
				}
			}
			
			Camera.animSpeed = 0f;
			if (speed != 0) {
				Camera.animSpeed = 4.5f;
				if (CTRL) {
					speed /= 2;
					Camera.animSpeed = 2.25f;
				}
				else if (RUN) {
					Camera.animSpeed = 15f;
				}
				
				if (!entity.isGrounded()) {
					Camera.animSpeed = 0f;
				}
	
				if (entity.isSubmerged()) {
					speed = waterAccel;
				}
				else if (!entity.isGrounded()) {
					speed = airAccel;
				}
	
				direction *= Math.PI / 180f;
				getEntity().accelerate(new Vector3f(-(float) Math.sin(direction), 0, (float) Math.cos(direction)), speed);
			}
		}
	
		if (CTRL) {
			Camera.offsetY -= Window.deltaTime * CROUCH_SPEED;
			if (Camera.offsetY < CAMERA_CROUCHING_HEIGHT)
				Camera.offsetY = CAMERA_CROUCHING_HEIGHT;
		} else {
			Camera.offsetY += Window.deltaTime * CROUCH_SPEED;
			if (Camera.offsetY > CAMERA_STANDING_HEIGHT)
				Camera.offsetY = CAMERA_STANDING_HEIGHT;
		}
		
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			camera.getPosition().set(getEntity().pos.x, getEntity().pos.y + Camera.offsetY,
					getEntity().pos.z);
			
			if (entity.isGrounded() && !entity.isFullySubmerged() && (W || A || S || D)) {
				if (walkSfxTimer >=.15f) {
					walkSfxTimer = 0f;
				}
				
				if (walkSfxTimer == 0f) {
					switch(entity.getMaterialStandingOn()) {
					default:
						//AudioHandler.play("walk_asphalt");
					}
					
				}
				
				walkSfxTimer += Window.deltaTime*.2f;
				
				
			} else {
				walkSfxTimer = 0f;
			}
		} else {
			Vector3f oldCamPos = camera.getPrevPosition();
			//entity.vel.set(Vector3f.sub(camera.getPosition(), oldCamPos).div(Window.deltaTime));
			entity.vel.zero();
			entity.pos.set(oldCamPos.x, oldCamPos.y - Camera.offsetY, oldCamPos.z);
			entity.previouslyGrounded = true;
		}
		
	}
	
	private static void climbingPhysics(Scene scene) {
		float pitch = scene.getCamera().getPitch();
		
		boolean A = Input.isDown("walk_left"),
				D = Input.isDown("walk_right"),
				W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"),
				JUMP = Input.isDown("jump");
		
		if ((W || A || D) && !S) {
			entity.accelerate(Vector3f.Y_AXIS, accelSpeed * (pitch <= 0 ? 1 : -1));
		} else if (S) {
			entity.accelerate(Vector3f.Y_AXIS, accelSpeed * (pitch <= 0 ? -1 : 1));
		}
		
		if (JUMP) {
			Vector3f dir = new Vector3f(scene.getCamera().getDirectionVector());
			entity.vel.x = dir.x * entity.vel.y;
			entity.vel.z = dir.z * entity.vel.y;
			entity.jump(jumpVel);
		}
	}
	
	private static void waterPhysics(Scene scene) {
		float forwardSpeed = 0, strafeSpeed = 0;
		
		boolean A = Input.isDown("walk_left"),
				D = Input.isDown("walk_right"),
				W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"),
				JUMP = Input.isDown("jump"),
				CROUCH = Input.isDown("sneak");
		
		if (A && D) {
			if (!entity.vel.isZero()) {
				entity.vel.mul(.92f);
			}
		} else if (A && !D) {
			strafeSpeed = 60;
		} else if (!A && D) {
			strafeSpeed = -60;
		}
		
		if (W && S) {
			if (!entity.vel.isZero()) {
				entity.vel.mul(.92f);
			}
		} else if (W && !S) {
			forwardSpeed = -60;
			
		} else if (!W && S) {
			forwardSpeed = 60;
		}
		
		if (Input.isPressed(Keyboard.KEY_R) && entity.isFullySubmerged()) {
			forwardSpeed = -4000;
		}
		
		if (JUMP && !CROUCH) {
			entity.accelerate(Vector3f.Y_AXIS, 60);
		} else if (!JUMP && CROUCH) {
			entity.accelerate(Vector3f.Y_AXIS, -60);
		}
		
		final Vector3f forward = MathUtil.getDirection(scene.getCamera().getViewMatrix());
		final float yawRad = (float) Math.toRadians(scene.getCamera().getYaw());
		final Vector3f strafe = new Vector3f(-(float) Math.sin(yawRad), 0, (float) Math.cos(yawRad)).perpindicular();
		
		if (!entity.isFullySubmerged()) {
			forward.y = Math.max(forward.y, 0f);
			strafe.y = Math.max(strafe.y, 0f);
		}
		
		entity.accelerate(forward, forwardSpeed);
		entity.accelerate(strafe, strafeSpeed);
	}
	
	public static void disable() {
		disabled = true;
		entity.deactivated = true;
	}
	
	public static void enable() {
		disabled = false;
		entity.deactivated = false;
	}
}
