package dev;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import core.App;
import geom.AxisAlignedBBox;
import gl.Camera;
import gl.Render;
import gl.Window;
import gr.zdimensions.jsquish.Squish;
import gr.zdimensions.jsquish.Squish.CompressionMethod;
import gr.zdimensions.jsquish.Squish.CompressionType;
import map.architecture.Architecture;
import map.architecture.components.ArcClip;
import map.architecture.components.ClipType;
import scene.mapscene.MapScene;
import ui.UI;

public class EnvMapBuilder {
	
	private static final int WIDTH = 64, HEIGHT = 64;
	
	// Yaw, Pitch
	
	private static final int[] DIRECTIONS = new int[] {
			// +X,-X,+Y,-Y,+Z,-Z
			//180,0, 0,0, 0,90, 0,-90, 90,0, 270,0
			// +Z -Z -Y +Y +X -X
			90,0, 270,0, 0,-90, 0,90, 0,0, 180,0
	};
	
	public static void build_environment_maps() {
		if (!(App.scene instanceof MapScene))
			return;

		MapScene scene = (MapScene)App.scene;
		Architecture arc = scene.getArchitecture();

		File outputFile = new File("maps/" + arc.getMapName() + ".emp"); // EMP : EnviroMent maP
		
		UI.hideUI = true;
		
		try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile))) {
			outputStream.writeChars("EMP");
			outputStream.writeByte(1);
			
			outputStream.writeShort(WIDTH);
			outputStream.writeShort(HEIGHT);
			
			ArcClip[] clips = arc.bsp.clips;
			
			for(int i = 0; i < clips.length; i++) {
				ArcClip clip = clips[i];
				if (clip.id != ClipType.ENVIRONMENT_MAP)
					continue;
				
				buildEnvironmentMap(clip, i, outputStream);
			}
			
			outputStream.writeShort(-1);
			outputStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		UI.hideUI = false;
	}

	private static void buildEnvironmentMap(ArcClip clip, int clipIndex, DataOutputStream outputStream) throws IOException {
		AxisAlignedBBox bbox = clip.bbox;
		Vector3f center = bbox.getCenter();
		Camera camera = App.scene.getCamera();
		
		camera.getPosition().set(center);
		camera.clearEffectRotations();
		
		final int windowWidth = Window.getWidth();
		final int windowHeight = Window.getHeight();
		
		outputStream.writeShort(clipIndex);
		
		float scaleDownX = windowWidth / WIDTH;
		float scaleDownY = windowHeight / HEIGHT;
		int strideX = (int)scaleDownX;
		int strideY = (int)scaleDownY;
		boolean stutterX = (scaleDownX - strideX != 0f);
		boolean stutterY = (scaleDownX - strideY != 0f);
		
		byte[] arr = new byte[WIDTH * HEIGHT * 4];
		
		for(int i = 0; i < DIRECTIONS.length; i += 2) {
			camera.setYaw(DIRECTIONS[i]);
			camera.setPitch(DIRECTIONS[i + 1]);
			camera.updateProjection(256,256);
			camera.updateViewMatrix();

			ByteBuffer buffer = BufferUtils.createByteBuffer(windowWidth * windowHeight * 4);
			//Render.sceneOnlyRenderPass(Application.scene);
			Render.renderPass(App.scene);
			GL11.glReadPixels(0, 0, windowWidth, windowHeight, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buffer);
			
			int arrPos = 0;
			buffer.get(arr);
			
			for(int j = HEIGHT-1; j != 0; j--) {
				for(int k = 0; k < WIDTH; k++) {
					int x = (k * strideX) + (stutterX ? k % 2 : 0);
					int y = (j * strideY) + (stutterY ? j % 2 : 0);
					int index = (x + (y * windowWidth)) * 4;
					buffer.position(index);
					arr[arrPos++] = buffer.get();
					arr[arrPos++] = buffer.get();
					arr[arrPos++] = buffer.get();
					arr[arrPos++] = buffer.get();
				}
			}
			
			byte[] compData = Squish.compressImage(arr, WIDTH, HEIGHT, null, CompressionType.DXT1, CompressionMethod.CLUSTER_FIT);
			outputStream.writeInt(compData.length);
			outputStream.write(compData);
		}
		

		camera.updateProjection();
	}
}
