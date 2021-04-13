package gl;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import dev.Console;
import geom.Frustum;
import io.Input;
import scene.entity.Entity;
import util.MathUtil;

public class Camera {
	private static final float MAX_PITCH = 90;

	private static float zoom, targetZoom, zoomSpeed;

	public static float cameraSpeed = 80f, offsetY = 0, animSpeed = 0;

	public static int fov = 90;

	public static float mouseSensitivity = 1f;
	public static final float FAR_PLANE = 8000f;

	public static final float NEAR_PLANE = .1f;
	public static final byte NO_CONTROL = 0, SPECTATOR = 1, FIRST_PERSON = 2;
	
	private static double movementCounter = 0f;
	
	public static float swayFactor = 1f;
	
	private static Matrix4f createProjectionMatrix() {
		final Matrix4f projectionMatrix = new Matrix4f();
		final float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		final float y_scale = (float) (1f / Math.tan(Math.toRadians((fov - zoom) / 2f)));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -(2 * NEAR_PLANE * FAR_PLANE / frustum_length);
		projectionMatrix.m33 = 0;
		return projectionMatrix;
	}

	private Matrix4f projectionMatrix;
	private final Matrix4f projectionViewMatrix = new Matrix4f();
	private final Matrix4f viewMatrix = new Matrix4f();
	private final Vector3f position = new Vector3f();
	private final Vector3f prevPosition = new Vector3f();
	private final Frustum frustum = new Frustum();
	private float rawYaw, effectedYaw;		// Raw = only the camera's direction, whereas the effected
	private float rawPitch, effectedPitch;	// variables include the camera's looking direction, which
	private float rawRoll, effectedRoll;	// includes effects such as shaking and flinching

	private float shakeTime = 0f, shakeIntensity = 0f;
	private float flinchTime = 0f, flinchIntensity = 0f;
	private Vector3f flinchDir = new Vector3f();
	private float swayTime = 0f, swayIntensity = 0f, swaySpeed = 0f;
	private Vector3f swayDir = new Vector3f(), swayTarget = new Vector3f();

	private final Vector2f screenShake = new Vector2f();
	private final Vector3f flinch = new Vector3f();
	
	private Vector3f lookAt = null;
	private Vector3f viewDirection = new Vector3f();

	private Entity focus = null;

	private final boolean mouseIsGrabbed = false;

	private byte controlStyle = FIRST_PERSON;

	public Camera() {
		updateProjection();
	}

	public void addPitch(float f) {
		rawPitch += f;
	}

	public void addYaw(float f) {
		rawYaw += f;
	}

	private void clampPitch() {
		if (rawPitch < -MAX_PITCH) {
			rawPitch = -MAX_PITCH;
		} else if (rawPitch > MAX_PITCH) {
			rawPitch = MAX_PITCH;
		}
	}

	public void focusOn(Entity focus) {
		this.focus = focus;
		if (focus == null) {
			lookAt = null;
		}
	}

	public byte getControlStyle() {
		return controlStyle;
	}

	public Vector3f getDirectionVector() {
		return viewDirection;
	}

	public Frustum getFrustum() {
		return frustum;
	}
	
	public Vector3f getPosition() {
		return position;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public Matrix4f getProjectionViewMatrix() {
		return projectionViewMatrix;
	}

	public Vector2f getScreenShake() {
		return screenShake;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public float getYaw() {
		return rawYaw;
	}
	
	public float getPitch() {
		return rawPitch;
	}
	
	public float getRoll() {
		return rawRoll;
	}
	
	public float getEffectedYaw() {
		return effectedYaw;
	}
	
	public float getEffectedPitch() {
		return effectedPitch;
	}
	
	public float getEffectedRoll() {
		return effectedRoll;
	}
	
	public Vector3f getPrevPosition() {
		return prevPosition;
	}

	public void grabMouse() {
		Input.requestMouseGrab();
	}

	private void handleControl() {
		// Yaw/pitch look
		if (controlStyle == SPECTATOR || controlStyle == FIRST_PERSON) {
			if (Mouse.isGrabbed()) {
				final float offset = 10f;
				final float pitchChange = Input.getMouseDY() * (mouseSensitivity / offset);
				final float angleChange = Input.getMouseDX() * (mouseSensitivity / offset);
				rawPitch -= pitchChange;
				rawYaw += angleChange;
				clampPitch();
			}
		}

		// WASD movement
		if (controlStyle == SPECTATOR && !Console.isVisible()) {
			final Vector3f forward = MathUtil.getDirection(viewMatrix);
			final float yawRad = (float) Math.toRadians(rawYaw);
			final Vector3f strafe = new Vector3f(-(float) Math.sin(yawRad), 0, (float) Math.cos(yawRad))
					.perpindicular();

			float speed = Input.isDown(Keyboard.KEY_LCONTROL) ? cameraSpeed / 4f : cameraSpeed;
			speed *= Window.deltaTime;

			if (Input.isDown("walk_forward")) {
				forward.mul(-speed);
			} else if (Input.isDown("walk_backward")) {
				forward.mul(speed);
			} else {
				forward.zero();
			}

			if (Input.isDown("walk_right")) {
				strafe.mul(-speed);
			} else if (Input.isDown("walk_left")) {
				strafe.mul(speed);
			} else {
				strafe.zero();
			}

			position.add(forward).add(strafe);
		}
	}

	public boolean isMouseGrabbed() {
		return mouseIsGrabbed;
	}

	public boolean isShaking() {
		return shakeTime == 0f;
	}
	
	public boolean isSwaying() {
		return swayTime == 0f;
	}

	public void move() {
		//if (Input.isDown("walk_left") || Input.isDown("walk_right") || Input.isDown("walk_forward") || Input.isDown("walk_backward")) {
			movementCounter += Window.deltaTime * animSpeed;
			if (movementCounter >= 2.0 * Math.PI) {
				movementCounter -= 2.0 * Math.PI;
			}
		//}
		
		prevPosition.set(position);
		if (Math.abs(targetZoom - zoom) > .2f) {
			zoom += zoomSpeed;
			updateProjection();
		}

		if (shakeTime > 0) {
			shakeTime = Math.max(shakeTime - Window.deltaTime, 0f);
			if (shakeTime == 0) {
				screenShake.zero();
			} else {
				screenShake.set(-(shakeIntensity / 2f) + (float) (Math.random() * shakeIntensity),
						-(shakeIntensity / 2f) + (float) (Math.random() * shakeIntensity));
			}
		}
		
		if (flinchTime > 0) {
			float pwr = flinchTime*flinchTime;
			
			flinchTime = Math.max(flinchTime - Window.deltaTime, 0f);
			if (flinchTime != 0) {
				if (flinchDir.x > .5f) {
					flinch.x = pwr * -flinchIntensity; 
				} else if (flinchDir.x < -.5f) {
					flinch.x = pwr * flinchIntensity; 
				}
				
				if (flinchDir.y > .5f) {
					flinch.y = pwr * -flinchIntensity;
				} else if (flinchDir.y < -.5f) {
					flinch.y = pwr * flinchIntensity;
				}
				
				if (flinchDir.z > .5f) {
					flinch.z = pwr * -flinchIntensity;
				} else if (flinchDir.z < -.5f) {
					flinch.z = pwr * flinchIntensity;
				}
				
				
				
			} else {
				flinch.zero();
			}
		}
		
		if (swayTime > 0) {
			swayTime = Math.max(swayTime - Window.deltaTime, 0f);
			if (swayTime == 0f) {
				swayTarget.zero();
			} else if (Vector3f.distanceSquared(swayDir, swayTarget) < .5) {
				float remainder = 1f;
				float w1 = (float) (Math.random() - 0.5);
				remainder -= w1;
				float w2 = (float) (Math.random() - 0.5) * remainder;
				remainder -= w2;
				float w3 = (float) (Math.random() - 0.5) * remainder;

				swayTarget.set(w1 * swayIntensity, w2 * swayIntensity, w3 * swayIntensity);
			}
		}
		swayDir.x += (swayTarget.x - swayDir.x)*Window.deltaTime*swaySpeed;
		swayDir.y += (swayTarget.y - swayDir.y)*Window.deltaTime*swaySpeed;
		swayDir.z += (swayTarget.z - swayDir.z)*Window.deltaTime*swaySpeed;


		if (controlStyle == NO_CONTROL && focus != null) {
			if (lookAt == null) {
				final Vector3f lookPos = new Vector3f(focus.pos);
				lookAt = Vector3f.sub(position, lookPos).normalize();
			}

			rawPitch = MathUtil.lerp(rawPitch, (float) Math.toDegrees(Math.asin(lookAt.y)), .05f);
			rawYaw = MathUtil.angleLerp(rawYaw, -(float) Math.toDegrees(Math.atan2(lookAt.x, lookAt.z)), .05f);
			//angleAroundPlayer = -(rawYaw - 360);
		} else {
			handleControl();

			rawYaw %= 360;
			rawYaw += 360;
			rawYaw %= 360;
		}

		updateViewMatrix();
	}

	public void setControlStyle(byte style) {
		this.controlStyle = style;
	}

	public void setPitch(float pitch) {
		this.rawPitch = pitch;
	}

	public void setPosition(Vector3f position) {
		this.position.set(position);
	}

	public void setRoll(float roll) {
		this.rawRoll = roll;
	}

	public void setYaw(float yaw) {
		this.rawYaw = yaw;
	}

	public void setZoom(float i) {
		targetZoom = i;
		zoomSpeed = (targetZoom - zoom) / 45;
		updateProjection();
	}

	public void shake(float time, float intensity) {
		this.shakeIntensity = intensity;
		this.shakeTime = time;
	}
	
	public void sway(float time, float intensity, float speed) {
		this.swayIntensity = intensity * swayFactor;
		this.swayTime = time;
		this.swaySpeed = speed;
	}

	public void ungrabMouse() {
		Input.requestMouseRelease();
	}

	public void updateProjection() {
		this.projectionMatrix = createProjectionMatrix();
	}

	public void updateViewMatrix() {
		viewMatrix.identity();

		effectedYaw = rawYaw + screenShake.x + flinch.x + swayDir.x;
		effectedPitch = rawPitch + screenShake.y + flinch.y + swayDir.y;
		effectedRoll = rawRoll + flinch.z + swayDir.z;
		
		viewMatrix.rotateX(effectedPitch);
		viewMatrix.rotateY(effectedYaw);
		viewMatrix.rotateZ(effectedRoll);
		final Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
		viewMatrix.translate(negativeCameraPos);

		viewDirection.x = -viewMatrix.m02;
		viewDirection.y = -viewMatrix.m12;
		viewDirection.z = -viewMatrix.m22;

		Matrix4f.mul(projectionMatrix, viewMatrix, projectionViewMatrix);

		frustum.update(projectionViewMatrix);
	}
	
	public Matrix4f createModelMatrix() {
		Matrix4f matrix = new Matrix4f();
		
		matrix.rotateX(effectedPitch);
		matrix.rotateY(effectedYaw);
		matrix.rotateZ(effectedRoll);
		final Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
		viewMatrix.translate(negativeCameraPos);
		return matrix;
	}
	
	public void updateViewMatrixRaw() {
		viewMatrix.identity();

		viewMatrix.rotateX(rawPitch);
		viewMatrix.rotateY(rawYaw);
		viewMatrix.rotateZ(rawRoll);
		final Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
		viewMatrix.translate(negativeCameraPos);

		viewDirection.x = -viewMatrix.m02;
		viewDirection.y = -viewMatrix.m12;
		viewDirection.z = -viewMatrix.m22;

		Matrix4f.mul(projectionMatrix, viewMatrix, projectionViewMatrix);

		frustum.update(projectionViewMatrix);
	}

	public void flinch(Vector3f attackDir, float flinchIntensity) {
		this.flinchIntensity = flinchIntensity * 3;
		flinchTime = .33f;
		
		Vector3f look = this.getDirectionVector();
		Vector2f v = new Vector2f(attackDir.x, attackDir.z);
		
		float forwardFlinch = Vector2f.dot(v, new Vector2f(look.x, look.z));
		float sideFlinch = 1f - forwardFlinch;
		v.perpendicular();
		float altFlinch = Vector2f.dot(v, new Vector2f(look.x, look.z));
		flinchDir.set(new Vector3f(sideFlinch, forwardFlinch, altFlinch));
	}
	
	public Matrix4f getViewModelMatrix(boolean ignorePitch) {
		Matrix4f m = new Matrix4f();
		
		if (ignorePitch)
			m.rotateX(getPitch());
		m.translate(0f, -2f-offsetY, -.5f + (float)Math.sin(movementCounter) * .15f);
		m.scale(2f);
		m.rotateY(90);
		
		return m;
	}
}
