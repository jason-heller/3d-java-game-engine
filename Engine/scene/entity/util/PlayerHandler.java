package scene.entity.util;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import gl.Camera;
import gl.Window;
import gl.light.DynamicLight;
import io.Input;
import map.architecture.Architecture;
import scene.PlayableScene;
import scene.Scene;
import scene.entity.object.HoldableEntity;
import util.MathUtil;

public class PlayerHandler {
	private static PlayerEntity entity;
	
	public static final float WALKER_SPEED_MULTIPLIER = 0.5f;

	public static float jumpVel = 20f;
	public static float runSpeedMultiplier = 24f;
	public static float maxSpeed = 15f, maxAirSpeed = 15f, maxWaterSpeed = 35f;
	public static float accelSpeed = 85f, airAccel = 5f, waterAccel = 32f;
	public static float maxSpeedCrouch = 4f;
	
	public static final float CAMERA_STANDING_HEIGHT = 5f;
	public static final float CAMERA_CROUCHING_HEIGHT = 3f;
	private static float lightStrength = 21f;
	
	private static float accel = accelSpeed;
	
	private static float walkSfxTimer = 0f;

	private static boolean disabled = false;
	private static boolean threatened = false;

	public static HoldableEntity holding = null;
	
	private static DynamicLight light = null;
	private static float flickerTimer = 0f;
	
	public static PhysicsEntity getEntity() {
		return entity;
	}

	private static void passPhysVars(boolean running) {
		final float runScale = (running ? runSpeedMultiplier : 1f);
		accel = accelSpeed;
		entity.maxSpeed = maxSpeed * runScale;
		entity.maxAirSpeed = maxAirSpeed * runScale;
		entity.maxWaterSpeed = maxWaterSpeed;
	}

	public static void setEntity(PlayerEntity entity) {
		Camera.offsetY = CAMERA_STANDING_HEIGHT;
		PlayerHandler.entity = entity;
		enable();
	}

	public static void update(Scene scene) {
		boolean RUN = Input.isDown("run");
		
		passPhysVars(RUN);
		
		if (disabled) {
			return;
		}
		
		final Camera camera = scene.getCamera();
		Architecture arc = ((PlayableScene)scene).getArchitecture();
		
		if (light != null) {
			light.getRotation().set(camera.getPitch(), camera.getYaw(), 0);
			light.getPosition().set(camera.getPosition());
			if (threatened) {
				flickerTimer += Window.deltaTime;
				if (flickerTimer > .75f) {
					light.setStrength(light.getStrength() == 0.25f ? lightStrength : 0.25f);
					flickerTimer = 0f;
				}
			}
		}
		
		float speed = 0;
		final float yaw = camera.getYaw();
		float direction = yaw;

		final boolean A = Input.isDown("walk_left"), D = Input.isDown("walk_right"), W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"), JUMP = Input.isDown("jump");
		final boolean CTRL = Input.isDown("sneak");
		
		if (Input.isPressed(Keyboard.KEY_F)) {
			if (light != null) {
				arc.removeLight(light);
				light = null;
			} else {
				light = arc.addLight(new Vector3f(camera.getPosition()),
						new Vector3f(camera.getPitch(), camera.getYaw(), 0), lightStrength);
			}
			
			AudioHandler.play("click");
		}

		if (entity.isSubmerged()) {
			waterPhysics(scene);
		} else if (entity.isClimbing()) {
			climbingPhysics(scene);
		} else {
			// Handle game logic per tick, such as movement etc
			if (A && D) {
			} else if (A) {
				direction = yaw + 90;
				speed = accel;
			} else if (D) {
				direction = yaw - 90;
				speed = accel;
			}
	
			if (W && S) {
			} else if (S && !getEntity().isSliding()) {
				if (direction != yaw) {
					direction += 45 * (direction > yaw ? -1f : 1f);
				}
	
				speed = accel;
			} else if (W && !getEntity().isSliding()) {
	
				if (direction != yaw) {
					direction -= 45 * (direction > yaw ? -1f : 1f);
				} else {
					direction = yaw + 180;
				}
	
				if (RUN && !CTRL && entity.grounded) {
					camera.sway(.1f, 5f, 5f);
				}
				
				speed = accel;
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
			Camera.offsetY -= Window.deltaTime * maxSpeedCrouch;
			if (Camera.offsetY < CAMERA_CROUCHING_HEIGHT)
				Camera.offsetY = CAMERA_CROUCHING_HEIGHT;
		} else {
			Camera.offsetY += Window.deltaTime * maxSpeedCrouch;
			if (Camera.offsetY > CAMERA_STANDING_HEIGHT)
				Camera.offsetY = CAMERA_STANDING_HEIGHT;
		}
		
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			camera.getPosition().set(getEntity().pos.x, getEntity().pos.y + Camera.offsetY,
					getEntity().pos.z);
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
			entity.accelerate(Vector3f.Y_AXIS, accel * (pitch <= 0 ? 1 : -1));
		} else if (S) {
			entity.accelerate(Vector3f.Y_AXIS, accel * (pitch <= 0 ? -1 : 1));
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
		//entity.deactivated = true;
	}
	
	public static void enable() {
		disabled = false;
		//entity.deactivated = false;
	}

	public static void setThreatened(boolean isThreatened) {
		threatened = isThreatened;
	}
}