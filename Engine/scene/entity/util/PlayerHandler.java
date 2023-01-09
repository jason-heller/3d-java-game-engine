package scene.entity.util;

import org.joml.Vector3f;

// TODO: Why is this separate from playerEntity anyway?

import audio.AudioHandler;
import core.App;
import gl.Camera;
import gl.Window;
import gl.light.DynamicLight;
import io.Input;
import map.architecture.Architecture;
import map.architecture.util.BspRaycast;
import scene.PlayableScene;
import scene.Scene;
import scene.entity.object.HoldableEntity;

public class PlayerHandler {
	private static PlayerEntity entity;
	
	public static final float WALKER_SPEED_MULTIPLIER = 0.5f;

	public static float jumpVel = 40f;
	public static float runSpeedMultiplier = 24f;
	public static float maxSpeed = 55f, maxAirSpeed = 55f, maxWaterSpeed = 55f;
	public static float accelSpeed = 400f, airAccel = 20f, waterAccel = 32f;
	public static float maxSpeedCrouch = 72f;
	
	public static final float CAMERA_STANDING_HEIGHT = 5f;
	public static final float CAMERA_CROUCHING_HEIGHT = 2f;
	private static float lightStrength = 21f;
	
	private static float accel = accelSpeed;

	private static boolean disabled = false;

	public static HoldableEntity holding = null;
	
	private static DynamicLight light = null;
	
	private static boolean crouching = false;
	
	public static final float BBOX_WIDTH = 2f, BBOX_HEIGHT = 6f;
	public static final float BBOX_CROUCH_DIFF = (CAMERA_STANDING_HEIGHT - CAMERA_CROUCHING_HEIGHT);
	
	private static float direction;
	
	public static SkatePhysicsEntity getEntity() {
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
		light = null;
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
		
		float speed = accel;

		final boolean A = Input.isDown("walk_left"), D = Input.isDown("walk_right"), W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"), JUMP = Input.isDown("jump");
		final boolean CTRL = Input.isDown("sneak");
		
		if (Input.isPressed("flashlight")) {
			if (light != null) {
				arc.removeLight(light);
				light = null;
			} else {
				light = arc.addLight(new Vector3f(), new Vector3f(), lightStrength);
				setLightPos(light);
			}
			
			AudioHandler.play("click");
		}
		
		if (S)
			speed = 0f;

		// Handle game logic per tick, such as movement etc
		if (A && D) {
		} else if (A) {
			if (entity.grounded) {
				direction -= Window.deltaTime * 240f;
			}
		} else if (D) {
			if (entity.grounded) {
				direction += Window.deltaTime * 240f;
			}
		}
		
		if ((getEntity().isGrounded() && getEntity().vel.y < 0) && JUMP) {
			getEntity().jump(jumpVel);
			if (CTRL) {
				getEntity().pos.y += Camera.offsetY - CAMERA_CROUCHING_HEIGHT;
			}
		}
		

		entity.getViewAngle().set(0f, -direction, 0f);
		
		Camera.animSpeed = 0f;
		if (speed != 0 && !entity.deactivated) {
			Camera.animSpeed = 4.5f;
			if (crouching) {
				speed /= 2;
				Camera.animSpeed = 2.25f;
			}
			else if (RUN && entity.grounded) {
				Camera.animSpeed = 15f;
			}
			
			if (!entity.isGrounded()) {
				Camera.animSpeed = 0f;
			}

			if (!entity.isGrounded()) {
				speed = airAccel;
			}
			
			entity.rot.y = -direction;
			double dRad = direction * Math.PI / 180.0;
			getEntity().accelerate(new Vector3f(-(float) Math.sin(dRad), 0, (float) Math.cos(dRad)), speed);
		}
	
		if (CTRL) {
			crouching = true;
			if (Camera.offsetY > CAMERA_CROUCHING_HEIGHT) {
				Camera.offsetY -= Window.deltaTime * maxSpeedCrouch;
				
				if (Camera.offsetY <= CAMERA_CROUCHING_HEIGHT) {
					Camera.offsetY = CAMERA_CROUCHING_HEIGHT;
					if (entity.getBBox().getBounds().y == BBOX_HEIGHT) {
						entity.getBBox().getBounds().y -= BBOX_CROUCH_DIFF;
						entity.getBBox().getCenter().y -= BBOX_CROUCH_DIFF/2f;
						entity.pos.y -= BBOX_CROUCH_DIFF/2f;
						if (entity.getBBox().getBounds().y == BBOX_HEIGHT - BBOX_CROUCH_DIFF)
							Camera.offsetY += BBOX_CROUCH_DIFF;
					}
				}
			}
		} else if (crouching) {
			if (entity.getBBox().getBounds().y == BBOX_HEIGHT - BBOX_CROUCH_DIFF) {

				if (entityBspRayAbove(arc) > BBOX_HEIGHT + BBOX_CROUCH_DIFF ) {
					if (Camera.offsetY < CAMERA_STANDING_HEIGHT + BBOX_CROUCH_DIFF) {
						Camera.offsetY += Window.deltaTime * maxSpeedCrouch;
						if (Camera.offsetY >= CAMERA_STANDING_HEIGHT + BBOX_CROUCH_DIFF) {
							Camera.offsetY = CAMERA_STANDING_HEIGHT;
							entity.getBBox().getBounds().y += BBOX_CROUCH_DIFF;
							entity.getBBox().getCenter().y += BBOX_CROUCH_DIFF/2f;
							entity.pos.y += BBOX_CROUCH_DIFF/2f;
							crouching = false;
						}
					}
				}
			} else {
				if (Camera.offsetY < CAMERA_STANDING_HEIGHT) {
					Camera.offsetY += Window.deltaTime * maxSpeedCrouch;
					if (Camera.offsetY >= CAMERA_STANDING_HEIGHT) {
						Camera.offsetY = CAMERA_STANDING_HEIGHT;
						crouching = false;
					}
				}
			}
		}
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			if (camera.getFocus() == entity) {
				camera.getPosition().set(getEntity().pos.x, getEntity().pos.y + Camera.offsetY,
						getEntity().pos.z);
			}
		} else if (camera.getControlStyle() == Camera.SPECTATOR) {
			Vector3f oldCamPos = camera.getPrevPosition();
			//entity.vel.set(Vector3f.sub(camera.getPosition(), oldCamPos).div(Window.deltaTime));
			entity.vel.zero();
			entity.pos.set(oldCamPos.x, oldCamPos.y - Camera.offsetY, oldCamPos.z);
			entity.previouslyGrounded = true;
		}
		
	}
	
	private static float entityBspRayAbove(Architecture arc) {
		final float bx = entity.getBBox().getBounds().x;
		final float bz = entity.getBBox().getBounds().z;
		BspRaycast tl = arc.raycast(new Vector3f(entity.pos.x - bx, entity.pos.y, entity.pos.z - bz), Vector3f.Y_AXIS);
		BspRaycast tr = arc.raycast(new Vector3f(entity.pos.x - bx, entity.pos.y, entity.pos.z + bz), Vector3f.Y_AXIS);
		BspRaycast bl = arc.raycast(new Vector3f(entity.pos.x + bx, entity.pos.y, entity.pos.z - bz), Vector3f.Y_AXIS);
		BspRaycast br = arc.raycast(new Vector3f(entity.pos.x + bx, entity.pos.y, entity.pos.z + bz), Vector3f.Y_AXIS);
		return 	Math.min(tl == null ? Float.POSITIVE_INFINITY : tl.getDistance(), 
				Math.min(tr == null ? Float.POSITIVE_INFINITY : tr.getDistance(),
				Math.min(bl == null ? Float.POSITIVE_INFINITY : bl.getDistance(),
						br == null ? Float.POSITIVE_INFINITY : br.getDistance())));
	}

	private static void setLightPos(DynamicLight light2) {
		Camera camera = App.scene.getCamera();
		Vector3f lookVec = camera.getDirectionVector();
		Vector3f pos = new Vector3f(camera.getPosition());
		pos.add(Vector3f.mul(Vector3f.cross(lookVec, Vector3f.Y_AXIS), 1f));
		light.getRotation().set(camera.getPitch(), camera.getYaw()-1, 0);
		light.getPosition().set(pos);
	}
	
	public static void disable() {
		disabled = true;
		//entity.deactivated = true;
	}
	
	public static void enable() {
		disabled = false;
		//entity.deactivated = false;
	}
}