package gl;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import dev.cmd.Console;
import geom.Frustum;
import io.Input;
import util.MathUtil;
import util.Matrices;
import util.ThirdPersonCameraController;
import util.Vectors;

public class Camera {
	private static final float MAX_PITCH = MathUtil.HALFPI;

	private static float zoom, targetZoom, zoomSpeed;

	public static float cameraSpeed = 300f, offsetY = 0, animSpeed = 0;

	public static int fov = 90;

	public static float mouseSensitivity = 1f;
	public static final float FAR_PLANE = 8000f;

	public static final float NEAR_PLANE = .1f;
	public static final byte NO_CONTROL = 0, SPECTATOR = 1, FIRST_PERSON = 2, THIRD_PERSON = 3;
	
	private static double movementCounter = 0f;

	private Matrix4f projectionMatrix;
	private final Matrix4f projectionViewMatrix = new Matrix4f();
	private final Matrix4f viewMatrix = new Matrix4f();
	private final Matrix4f viewModelMatrix = new Matrix4f();
	
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
	private final Vector3f viewDirection = new Vector3f();

	private CameraFollowable focus = null;

	private final boolean mouseIsGrabbed = false;

	private byte controlStyle = NO_CONTROL;

	public Camera() {
		updateProjection();
	}

	private void handleControl() {
		// Yaw/pitch look
		if (controlStyle == SPECTATOR || controlStyle == FIRST_PERSON) {
			if (Mouse.isGrabbed()) {
				final float offset = 200f;
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

			final Vector3f strafe = new Vector3f((float) Math.cos(rawYaw), 0, (float) Math.sin(rawYaw));
			//strafe.orthogonalize(Vectors.POSITIVE_Y);

			float speed = Input.isDown(Keyboard.KEY_LCONTROL) ? cameraSpeed / 4f : cameraSpeed;
			speed *= Window.deltaTime;

			if (Input.isDown(Keyboard.KEY_W)) {
				forward.mul(-speed);
			} else if (Input.isDown(Keyboard.KEY_S)) {
				forward.mul(speed);
			} else {
				forward.zero();
			}

			if (Input.isDown(Keyboard.KEY_D)) {
				strafe.mul(speed);
			} else if (Input.isDown(Keyboard.KEY_A)) {
				strafe.mul(-speed);
			} else {
				strafe.zero();
			}

			position.add(forward).add(strafe);
		}
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
			} else if (swayDir.distanceSquared(swayTarget) < .5) {
				float remainder = 1f;
				float w1 = (float) (Math.random() - 0.5);
				remainder -= w1;
				float w2 = (float) (Math.random() - 0.5) * remainder;
				remainder -= w2;
				float w3 = (float) (Math.random() - 0.5) * remainder;

				swayTarget.set(w1 * swayIntensity, w2 * swayIntensity, w3 * swayIntensity);
			}
		}
		swayDir.x += (swayTarget.x - swayDir.x) * Window.deltaTime * swaySpeed;
		swayDir.y += (swayTarget.y - swayDir.y) * Window.deltaTime * swaySpeed;
		swayDir.z += (swayTarget.z - swayDir.z) * Window.deltaTime * swaySpeed;

		if (controlStyle == THIRD_PERSON && focus != null) {
			position.set(focus.getPosition());
			lookAt = focus.getViewAngle();
			rawPitch = (float) (Math.asin(lookAt.y));
			rawYaw = -(float) (Math.atan2(lookAt.x, lookAt.z));
		}

		if (controlStyle == NO_CONTROL && focus != null) {
			final Vector3f lookPos = new Vector3f(focus.getPosition());
			lookAt = Vectors.sub(position, lookPos).normalize();
			rawPitch = MathUtil.lerp(rawPitch, (float) (Math.asin(lookAt.y)), 1f);
			rawYaw = MathUtil.angleLerp(rawYaw, -(float) Math.toDegrees(Math.atan2(lookAt.x, lookAt.z)), 1f);
		} else {
			handleControl();
		}

		updateViewMatrix();
	}

	public void setControlStyle(byte style) {
		this.controlStyle = style;
	}
	
	public void setControlStyle(byte style, CameraFollowable focus) {
		this.controlStyle = style;
		this.focus = (style == THIRD_PERSON) ? new ThirdPersonCameraController(focus) : focus;
	}

	public void setPitch(float pitch) {
		this.rawPitch = pitch;
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
		this.swayIntensity = intensity;
		this.swayTime = time;
		this.swaySpeed = speed;
	}

	public void ungrabMouse() {
		Input.requestMouseRelease();
	}

	public void updateProjection() {
		updateProjection((float) Display.getWidth(), (float) Display.getHeight());
	}
	
	public void updateProjection(float width, float height) {
		projectionMatrix = new Matrix4f();
		final float aspectRatio = width / height;
		final float y_scale = (float) (1f / Math.tan(Math.toRadians((fov - zoom) / 2f)));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00(x_scale);
		projectionMatrix.m11(y_scale);
		projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustum_length));
		projectionMatrix.m23(-1);
		projectionMatrix.m32(-(2 * NEAR_PLANE * FAR_PLANE / frustum_length));
		projectionMatrix.m33(0);
	}

	public void clearEffectRotations() {
		screenShake.zero();
		flinch.zero();
		swayDir.zero();
	}
	
	public void updateViewMatrix() {
		viewMatrix.identity();

		effectedYaw = rawYaw + screenShake.x + flinch.x + swayDir.x;
		effectedPitch = rawPitch + screenShake.y + flinch.y + swayDir.y;
		effectedRoll = rawRoll + flinch.z + swayDir.z;

		viewMatrix.rotateZ(effectedRoll);
		viewMatrix.rotateX(effectedPitch);
		viewMatrix.rotateY(effectedYaw);
		viewMatrix.translate(-position.x, -position.y, -position.z);

		viewDirection.x = -viewMatrix.m02();
		viewDirection.y = -viewMatrix.m12();
		viewDirection.z = -viewMatrix.m22();

		Matrices.mul(projectionMatrix, viewMatrix, projectionViewMatrix);

		frustum.update(projectionViewMatrix);
		
		viewModelMatrix.identity();
		viewModelMatrix.translate(position);
		viewModelMatrix.rotateZ(-effectedRoll);
		viewModelMatrix.rotateY(-effectedYaw);
		viewModelMatrix.rotateX(-effectedPitch);
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
	
	void updateViewMatrixRaw() {
		viewMatrix.identity();

		viewMatrix.rotateX(rawPitch);
		viewMatrix.rotateY(rawYaw);
		viewMatrix.rotateZ(rawRoll);
		viewMatrix.translate(-position.x, -position.y, -position.z);

		viewDirection.x = -viewMatrix.m02();
		viewDirection.y = -viewMatrix.m12();

		Matrices.mul(projectionMatrix, viewMatrix, projectionViewMatrix);

		frustum.update(projectionViewMatrix);
	}

	public void flinch(Vector3f attackDir, float flinchIntensity) {
		this.flinchIntensity = flinchIntensity * 3;
		flinchTime = .33f;
		
		Vector3f look = this.getDirectionVector();
		Vector2f v = new Vector2f(attackDir.x, attackDir.z);
		
		float forwardFlinch = v.dot(new Vector2f(look.x, look.z));
		float sideFlinch = 1f - forwardFlinch;
		v.perpendicular();
		float altFlinch = v.dot(new Vector2f(look.x, look.z));
		flinchDir.set(new Vector3f(sideFlinch, forwardFlinch, altFlinch));
	}

	private void clampPitch() {
		if (rawPitch < -MAX_PITCH) {
			rawPitch = -MAX_PITCH;
		} else if (rawPitch > MAX_PITCH) {
			rawPitch = MAX_PITCH;
		}
	}

	public void setFocus(CameraFollowable focus) {
		this.focus = focus;
		if (focus == null) {
			lookAt = null;
		}
	}

	public byte getControlStyle() {
		return controlStyle;
	}

	public Vector3f getDirectionVector() {
		return new Vector3f(viewDirection);
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
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public Matrix4f getProjectionViewMatrix() {
		return projectionViewMatrix;
	}

	public Vector2f getScreenShake() {
		return screenShake;
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
	
	public Matrix4f getViewModelMatrix() {
		return viewModelMatrix;
	}

	public CameraFollowable getFocus() {
		return focus;
	}
	
	public void addPitch(float f) {
		rawPitch += f;
	}

	public void addYaw(float f) {
		rawYaw += f;
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
}
