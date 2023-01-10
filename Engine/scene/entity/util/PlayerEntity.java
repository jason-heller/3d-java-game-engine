package scene.entity.util;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import dev.cmd.Console;
import geom.Plane;
import gl.Camera;
import gl.CameraFollowable;
import gl.Window;
import gl.anim.Animator;
import gl.line.LineRender;
import io.Input;
import scene.PlayableScene;
import util.Colors;
import util.GeomUtil;

/**
 * @author Jason
 *
 */
public class PlayerEntity extends SkatePhysicsEntity implements CameraFollowable {
	
	////////////////////////////////
	
	public static final float CAMERA_STANDING_HEIGHT = 5f;

	public static final float BBOX_WIDTH = 2f, BBOX_HEIGHT = 6f;

	private static final float RAIL_GRAVITATION = 3f;
	
	////////////////////////////////
	
	private Camera camera;
	
	private static int hp = 15;
	private static int maxHp = 15;
	
	private static float direction;
	
	public static boolean isEnabled = false;
	
	private Vector3f viewAngle = new Vector3f();
	
	public PlayerEntity(Camera camera) {
		super("player", new Vector3f(BBOX_WIDTH, BBOX_HEIGHT, BBOX_WIDTH));
		this.camera = camera;
		this.setModel("untitled");
		this.setAnimator(new Animator(getModel().getSkeleton(), this));
		getAnimator().loop("idle");
		
	}
	
	@Override
	public void update(PlayableScene scene) {
		if (!isEnabled)
			return;
		
		if (camera.getControlStyle() == Camera.SPECTATOR) {
			return;
		}
		
		super.update(scene);

		Vector3f boardPos = Vector3f.add(pos, new Vector3f(0, -BBOX_HEIGHT, 0));

		final boolean A = Input.isDown("walk_left"),
				GRIND = Input.isPressed(Keyboard.KEY_Z),
				D = Input.isDown("walk_right"),
				S = Input.isDown("walk_backward"), 
				JUMP = Input.isPressed("jump");
		
		// Architecture arc = ((PlayableScene)scene).getArchitecture();
		
		float speed = accelSpeed;
		
		if (GRIND && grindRail == null) {
			grindRail = scene.getArchitecture().getNearestRail(boardPos, vel, RAIL_GRAVITATION);

			if (grindRail != null) {
				Vector3f edge =  Vector3f.sub(grindRail.end, grindRail.start);
				railLengthSqr = edge.lengthSquared();
				Vector3f edgeNormal = edge.normalize();
				Vector3f newPoint = GeomUtil.projectPointOntoLine(boardPos, grindRail.start, edgeNormal);
				
				
				grindOrigin = grindRail.start;
				grindSpeed = vel.length();
				
				if (vel.dot(edgeNormal) < 0) {
					edgeNormal.negate();
					grindOrigin = grindRail.end;
				}
				
				grindNormal = edgeNormal;
				pos.set(newPoint.x, newPoint.y + BBOX_HEIGHT, newPoint.z);
			}
		}
		
		if (grindOrigin != null) {
			LineRender.drawBox(grindOrigin, new Vector3f(1,1,1), Colors.WHITE);
			LineRender.drawLine(grindOrigin, Vector3f.add(grindOrigin, Vector3f.mul(grindNormal, (float)Math.sqrt(railLengthSqr))));
		}
		
		LineRender.drawBox(boardPos, new Vector3f(RAIL_GRAVITATION,RAIL_GRAVITATION,RAIL_GRAVITATION), Colors.RED);
		
		if (S)
			speed = 0f;

		// Handle game logic per tick, such as movement etc
		if (grounded) {
			if (A && D) {
			} else if (A) {
				direction -= Window.deltaTime * 240f;
				getAnimator().start("turn_l");
			} else if (D) {
				direction += Window.deltaTime * 240f;
				getAnimator().start("turn_r");
			}
			
			if ((vel.y < 0 || grindRail != null) && JUMP) {
				jump(jumpVel);
				getAnimator().start("ollie");
				grindRail = null;
			}
		}
		
		if (!previouslyGrounded && grounded) {
			getAnimator().start("land");
		}
		
		viewAngle.set(0f, -direction, 0f);
		
		Camera.animSpeed = 0f;
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			if (camera.getFocus() == this) {
				camera.getPosition().set(pos.x, pos.y + Camera.offsetY, pos.z);
			}
		}
		
		if (speed != 0 && !deactivated) {
			Camera.animSpeed = 4.5f;
			if (!grounded) {
				Camera.animSpeed = 0f;
				speed = airAccel;
			}
			
			rot.y = -direction;
			double dRad = direction * Math.PI / 180.0;
			accelerate(new Vector3f(-(float) Math.sin(dRad), 0, (float) Math.cos(dRad)), speed);
		}
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
}
