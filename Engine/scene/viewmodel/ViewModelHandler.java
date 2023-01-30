package scene.viewmodel;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import core.App;
import gl.Camera;
import gl.Render;
import gl.Window;
import gl.res.Mesh;
import gl.res.Texture;
import gl.res.TexturedModel;
import gl.res.mesh.MeshData;
import scene.PlayableScene;
import util.MathUtil;
import util.Matrices;
import util.Vectors;

public class ViewModelHandler {
	public static final int MAX_MODELS = 4;
	private ViewModel[] drawnModels;
	
	private Vector3f lastCamPos = new Vector3f();
	private Vector3f sway = new Vector3f();
	
	private float lastYaw, lastPitch, swayYaw, swayPitch;
	
	private float swayTimer = 0f;
	
	private final float VIEWMDL_ORIG_X = 1.81f - 0.107f;
	private final float VIEWMDL_ORIG_Y = -0.7f;
	private final float VIEWMDL_ORIG_Z = -1.5f;
	private final float VIEWMDL_SCALE = 1f / 8f;
	private final float VIEWMDL_SINK_POS = -.75f;
	private static final float VIEWMDL_LOOKSWAY_RECOVER = 5f;
	private static final float VIEWMDL_FALLSWAY_RECOVER = .6f;
	private static final float VIEWMDL_SINK_SPEED = .25f;
	
	public static float viewmodelSwayDuration = 2f;
	public static float viewmodelTurnSway = .2f;
	public static float viewmodelSwayAmount = .04f;
	
	private Matrix4f camMatrix = new Matrix4f();
	
	public ViewModelHandler() {
		drawnModels = new ViewModel[MAX_MODELS];
		
		Camera camera = App.scene.getCamera();
		lastYaw = camera.getEffectedYaw();
		lastPitch = camera.getEffectedPitch();
		lastCamPos = camera.getPosition();
		
		MeshData.setField("screen", "screen");
	}
	
	public void render(PlayableScene scene) {

		Camera camera = App.scene.getCamera();
		
		if (camera.getFocus() != scene.getPlayer()) {
			return;
		}
		
		Matrix4f m = new Matrix4f();
		
		lastCamPos.y = camera.getPosition().y;
		float len = Vectors.distanceSquared(camera.getPosition(), lastCamPos);

		if (len > 0.01f) {
			swayTimer += Window.deltaTime;
			double ms = ((swayTimer % viewmodelSwayDuration) / viewmodelSwayDuration) * MathUtil.TAU;
			sway.x = (float)Math.sin(ms) * viewmodelSwayAmount;
			sway.z = (float)Math.sin(ms) * viewmodelSwayAmount;
		}
		 if (!scene.getPlayer().isGrounded()) {
			sway.y = MathUtil.lerp(sway.y, VIEWMDL_SINK_POS, Window.deltaTime * VIEWMDL_SINK_SPEED);
		} else {
			sway.y = Math.min(0f, sway.y + (Window.deltaTime * VIEWMDL_FALLSWAY_RECOVER));
		}
		swayYaw += MathUtil.angleDifference(lastYaw, camera.getEffectedYaw()) * Window.deltaTime * viewmodelTurnSway;
		swayPitch += MathUtil.angleDifference(lastPitch, camera.getEffectedPitch()) * Window.deltaTime * viewmodelTurnSway;

		swayYaw = MathUtil.lerp(swayYaw, 0f, Window.deltaTime * VIEWMDL_LOOKSWAY_RECOVER);
		swayPitch = MathUtil.lerp(swayPitch, 0f, Window.deltaTime * VIEWMDL_LOOKSWAY_RECOVER);
		
		m.translate(VIEWMDL_ORIG_X + sway.x + swayYaw, VIEWMDL_ORIG_Y + sway.y + swayPitch, VIEWMDL_ORIG_Z + sway.z);
		m.scale(VIEWMDL_SCALE);
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		lastYaw = camera.getEffectedYaw();
		lastPitch = camera.getEffectedPitch();
		lastCamPos.set(camera.getPosition());
		
		camMatrix.identity();
		Vector3f pos = new Vector3f(camera.getPosition());
		pos.add(camera.getDirectionVector());
		camMatrix.translate(pos);
		camMatrix.rotateZ(-camera.getEffectedRoll());
		camMatrix.rotateY(-camera.getEffectedYaw());
		camMatrix.rotateX(-camera.getEffectedPitch());
		camMatrix.translate(Vectors.mul(new Vector3f(sway.x + swayYaw, sway.y + swayPitch, sway.z), .7f));
		Vector3f[] lights = scene.getArchitecture().getLightsAt(camera.getPosition());
		
		for(int i = 0; i < MAX_MODELS; i++) {
			if (drawnModels[i] == null) 
				continue;
			drawnModels[i].update();
			TexturedModel viewModel = drawnModels[i].getTexturedModel();
			Mesh model = viewModel.getModel();
			Texture texture = viewModel.getTexture();
			Matrix4f matrix = viewModel.getMatrix();
			
			model.getMeshData().update(model, texture, camMatrix);
			Render.renderViewModel(model, texture, Matrices.mul(m, matrix), lights);

		}
	}
	
	public void clearSway() {
		sway.zero();
		swayYaw = 0f;
		swayPitch = 0f;
	}
	
	public void clearAllModels() {
		for(int i = 0; i < MAX_MODELS; i++) {
			drawnModels[i] = null;
		}
	}

	public void setDrawnModel(int index, ViewModel viewModel) {
		drawnModels[index] = viewModel;
	}
	
	public void removeDrawnModel(int index) {
		drawnModels[index] = null;
	}

	public ViewModel getDrawnModel(int i) {
		return drawnModels[i];
	}
}
