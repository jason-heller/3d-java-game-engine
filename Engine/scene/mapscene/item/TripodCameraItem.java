package scene.mapscene.item;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import dev.Console;
import gl.Camera;
import gl.Window;
import gl.post.NightVisionShader;
import gl.post.PostProcessing;
import gl.res.mesh.MeshData;
import io.Input;
import map.architecture.Architecture;
import map.architecture.functions.commands.CamView;
import map.architecture.util.BspRaycast;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.object.TripodCameraEntity;
import scene.entity.util.PlayerHandler;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import ui.UI;
import util.Colors;

public class TripodCameraItem extends Item {
	
	private int numCameras = 0;
	private int visCam = 0;
	private TripodCameraEntity[] cameras;
	private Entity lastFocus = null;
	private byte lastControlStyle = 0;
	private Vector3f lastRot = new Vector3f();
	
	private float noiseAmplifier = 1f;
	
	private boolean isViewingCams = false;
	private static final int MAX_CAMERAS = 2;
	
	public static final float STATIC_BROKEN_AMPLIFIER = 5f;
	
	private Camera camera;
	
	public TripodCameraItem(MapScene scene) {
		super(scene);
		MeshData.setField("tabletView", "none");
		MeshData.setField("tabletCamTitle", "===CAMERA FEEDS===");
		MeshData.setField("tabletCamInfo1", "#SCAM01: READY");
		MeshData.setField("tabletCamInfo2", "#SCAM02: READY");
		cameras = new TripodCameraEntity[MAX_CAMERAS];
		camera = scene.getCamera();
	}

	@Override
	public void interact() {
		if (isViewingCams) {
			visCam++;
			if (visCam == this.numCameras)
				visCam = 0;
			
			setCamera();
		}
		else if (numCameras < MAX_CAMERAS) {
			Camera c = Application.scene.getCamera();
	
			Vector3f rayDir = new Vector3f(0, -1, 0);
			Vector3f rayOrigin = Vector3f.add(c.getPosition(), Vector3f.mul(c.getDirectionVector(), 4f));
			BspRaycast raycastData = scene.getArchitecture().raycast(rayOrigin, rayDir);
			if (raycastData != null && raycastData.getDistance() < 20f) {
				Vector3f normal = scene.getArchitecture().bsp.planes[raycastData.getFace().planeId].normal;
				if (normal.y > .75f) {
					Vector3f spawnPos = Vector3f.add(rayOrigin, Vector3f.mul(rayDir, raycastData.getDistance()-1f));
					TripodCameraEntity tripodCam = new TripodCameraEntity(spawnPos, normal, -c.getYaw());
					EntityHandler.addEntity(tripodCam);
					cameras[numCameras] = tripodCam;
					numCameras++;
					MeshData.setField("tabletCamInfo" + numCameras, "CAM0" + numCameras + ": ACTIVE");
				}
			}
		}
	}
	
	@Override
	public void interactEnd() {
	}

	@Override
	public ViewModel setViewModel() {
		Matrix4f m = new Matrix4f();
		return new ViewModel("tablet", "default", m);
	}

	@Override
	public boolean setHoldKeyToInteract() {
		return false;
	}

	@Override
	public void equip() {
	}

	@Override
	public void unequip() {
	}

	@Override
	public void update() {
		if (Input.isPressed("interact") && numCameras > 0) {
			if (!isViewingCams) {
				isViewingCams = true;
				startCameraView();
				setCamera();
			} else {
				isViewingCams = false;
				
				stopCameraView();
			}
		}
		
		if (isViewingCams) {
			UI.drawString("CAM0" + (1 + visCam), 640, 30, .4f, true);
		}
		
		if (noiseAmplifier > 1f && !cameras[visCam].isDamaged()) {
			noiseAmplifier = Math.max(noiseAmplifier - Window.deltaTime*2f, 1f);
			NightVisionShader.noiseAmplifier = noiseAmplifier;
		}
		
		for(int i = 0; i < numCameras; i++) {
			if (cameras[i].isDamaged()) {
				int camId = i + 1;
				MeshData.setField("tabletCamInfo" + camId, "#ACAM0" + camId + ": DAMAGED");
			}
		}
	}

	private void stopCameraView() {
		camera.setFocus(lastFocus);
		camera.setControlStyle(lastControlStyle);
		camera.setPitch(lastRot.x);
		camera.setYaw(lastRot.y);
		camera.setRoll(lastRot.z);
		PostProcessing.disable(PostProcessing.NIGHT_VISION);
		
		PlayerHandler.enable();
	}

	private void startCameraView() {
		lastFocus = camera.getFocus();
		lastControlStyle = camera.getControlStyle();
		lastRot.set(camera.getPitch(), camera.getYaw(), camera.getRoll());
		
		camera.setFocus(null);
		camera.setControlStyle(Camera.NO_CONTROL);
		PostProcessing.enable(PostProcessing.NIGHT_VISION);
		
		PlayerHandler.disable();
	}

	private void setCamera() {
		noiseAmplifier = cameras[visCam].isDamaged() ? STATIC_BROKEN_AMPLIFIER : 2.5f;
		NightVisionShader.noiseAmplifier = noiseAmplifier;
		
		AudioHandler.play("click");
		camera.getPosition().set(cameras[visCam].getViewPos());
		camera.setPitch(-cameras[visCam].rot.x);
		camera.setYaw(-cameras[visCam].rot.y);
		camera.setRoll(-cameras[visCam].rot.z);
	}
}
