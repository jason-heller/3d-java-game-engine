package scene.viewmodel;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import core.Application;
import gl.Camera;
import gl.Render;
import gl.TexturedModel;
import gl.res.Model;
import gl.res.Texture;
import gl.res.mesh.MeshData;
import scene.PlayableScene;

public class ViewModelHandler {
	private static final int MAX_MODELS = 4;
	private ViewModel[] drawnModels;
	
	private Vector3f pos, lastCamPos = new Vector3f();
	
	public ViewModelHandler() {
		drawnModels = new ViewModel[MAX_MODELS];
		
		MeshData.setField("screen", "screen");
	}
	
	public void render(PlayableScene scene) {

		Camera camera = Application.scene.getCamera();
		
		if (camera.getFocus() != scene.getPlayer()) {
			return;
		}
		
		Matrix4f m = new Matrix4f();
		float len = Vector3f.distanceSquared(camera.getPosition(), lastCamPos);
		m.translate(1.81f-.107f, -.7f, -1.5f);
		m.scale(1/8f);
		//matrix.translate(1-0.107f,-0.5f,-1.5f);
		//matrix.scale(SCALE);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		lastCamPos.set(camera.getPosition());
		
		Matrix4f camMatrix = new Matrix4f();
		Vector3f pos = new Vector3f(camera.getPosition());
		pos.add(Vector3f.mul(camera.getDirectionVector(), 48f));
		camMatrix.translate(pos);
		camMatrix.rotateZ(-camera.getEffectedRoll());
		camMatrix.rotateY(-camera.getEffectedYaw());
		camMatrix.rotateX(-camera.getEffectedPitch());
		Vector3f[] lights = scene.getArchitecture().getLightsAt(camera.getPosition());
		
		for(int i = 0; i < MAX_MODELS; i++) {
			if (drawnModels[i] == null) 
				continue;
			drawnModels[i].update();
			TexturedModel viewModel = drawnModels[i].getTexturedModel();
			Model model = viewModel.getModel();
			Texture texture = viewModel.getTexture();
			Matrix4f matrix = viewModel.getMatrix();
			
			model.getMeshData().update(model, texture, camMatrix);
			Render.renderViewModel(model, texture, Matrix4f.mul(m, matrix, null), lights);

		}
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
