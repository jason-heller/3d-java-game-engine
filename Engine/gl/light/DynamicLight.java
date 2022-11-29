package gl.light;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import gl.Window;
import gl.fbo.FBO;

public class DynamicLight {
	
	private Vector3f position;
	private Vector3f rotation;
	private Vector3f viewDirection;
	private float cutoff, outerCutoff;
	private float strength;
	private FBO fbo;
	
	private Matrix4f lightView;
	
	private int id;
	
	public DynamicLight(Vector3f position, Vector3f rotation, float strength, int id) {
		this.position = position;
		this.rotation = rotation;
		this.strength = strength;
		this.cutoff = (float) Math.cos(Math.toRadians(22.5f));
		this.outerCutoff = (float) Math.cos(Math.toRadians(27.5f));
		
		lightView = new Matrix4f();
		viewDirection = new Vector3f();
		updateLightView();
		
		this.id = id;
		
		fbo = new FBO(Window.getWidth(), Window.getHeight(), false, true);
		Resources.addTexture("shadow" + id, fbo.getDepthBuffer(), fbo.getWidth(), fbo.getHeight());
	}
	
	public void updateLightView() {
		lightView.identity();
		lightView.rotateX(rotation.x);
		lightView.rotateY(rotation.y);
		lightView.rotateZ(rotation.z);
		final Vector3f negPosition = new Vector3f(-position.x, -position.y, -position.z);
		lightView.translate(negPosition);
		
		viewDirection.x = -lightView.m02;
		viewDirection.y = -lightView.m12;
		viewDirection.z = -lightView.m22;
	}

	public void cleanUp() {
		Resources.removeTexture("shadow" + id);
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(Vector3f direction) {
		this.rotation = direction;
	}

	public float getCutoff() {
		return cutoff;
	}

	public void setCutoff(float cutoff) {
		this.cutoff = cutoff;
	}

	public float getOuterCutoff() {
		return outerCutoff;
	}

	public void setOuterCutoff(float outerCutoff) {
		this.outerCutoff = outerCutoff;
	}

	public float getStrength() {
		return strength;
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

	public FBO getFbo() {
		return this.fbo;
	}
	
	public int getShadowMap() {
		return fbo.getDepthBuffer();
	}

	public Matrix4f getLightViewMatrix() {
		return lightView;
	}
	
	public float getLightReachInUnits() {
		return strength * 8f;
	}
	
	public Vector3f getViewDirection() {
		return viewDirection;
	}
}
