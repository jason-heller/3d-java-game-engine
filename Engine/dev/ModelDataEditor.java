package dev;

import org.joml.Vector4f;
import org.lwjgl.input.Keyboard;

import core.Application;
import gl.Window;
import gl.res.mesh.ImageTag;
import gl.res.mesh.MeshData;
import gl.res.mesh.MeshTag;
import gl.res.mesh.TextTag;
import io.Input;
import scene.PlayableScene;
import scene.viewmodel.ViewModel;
import scene.viewmodel.ViewModelHandler;
import ui.UI;

public class ModelDataEditor {
	private ViewModelHandler handler;
	
	private MeshData meshData;
	private MeshTag meshTag;
	private int meshTagId = 0;
	
	private float heldTimer = 0f;
	private float offset = .1f;
	
	private boolean type = false;
	private String typedText = "";
	
	private Vector4f viewportOrig;
	
	private void editMeshTag() {
		boolean ctrl = Input.isDown(Keyboard.KEY_LCONTROL) || Input.isDown(Keyboard.KEY_RCONTROL);
		
		if (Input.isPressed(Keyboard.KEY_EQUALS)) {
			meshTagId++;
			if (meshTagId == meshData.getStructs().size()) {
				meshTagId = 0;
			}
		}
		if (Input.isPressed(Keyboard.KEY_MINUS)) {
			meshTagId--;
			if (meshTagId == -1) {
				meshTagId = meshData.getStructs().size() - 1;
			}
			
		}
		
		if (Input.getMouseDWheel() > 0) {
			if (ctrl) {
				if (offset <= .1f) {
					offset -= .025f;
				} else {
					offset -= .1f;
				}
			} else {
				meshTag.setScale(meshTag.getScale() - offset);
			}
		} else if (Input.getMouseDWheel() < 0) {
			if (ctrl) {
				if (offset <= .1f) {
					offset += .025f;
				} else {
					offset += .1f;
				}
			} else {
				meshTag.setScale(meshTag.getScale() + offset);
			}
		}
		
		if (Input.isDown(Keyboard.KEY_RIGHT)) {
			shiftTag(offset, 0f, 0f);
		} else if (Input.isDown(Keyboard.KEY_LEFT)) {
			shiftTag(-offset, 0f, 0f);
		} else if (Input.isDown(Keyboard.KEY_UP)) {
			shiftTag(0f, offset, 0f);
		} else if (Input.isDown(Keyboard.KEY_DOWN)) {
			shiftTag(0, -offset, 0f);
		} else {
			heldTimer = 0f;
		}
		
		UI.drawString("tag: " + meshTag.getClass().getSimpleName() + " (" + meshTagId + ")" + "\n" +
				"snap: " + offset +
				"\npos: " + meshTag.getOffset() +
				"\nscale: " + meshTag.getScale(), 2, 350);
		
		if (meshTag instanceof TextTag) {
			TextTag textTag = (TextTag)meshTag;
			
			if (Input.isPressed(Keyboard.KEY_C)) {
				textTag.setCentered(!textTag.isCentered());
			}
			
			if (Input.isPressed(Keyboard.KEY_HOME)) {
				if (type) {
					textTag.setField(typedText);
				} else {
					type = true;
					typedText = "";
				}
			}
			
			UI.drawString(
					"[C]entered: " + textTag.isCentered(), 2, 450);
		}
		
		if (meshTag instanceof ImageTag) {
			ImageTag imgTag = (ImageTag)meshTag;
			
			int[] viewport = imgTag.getViewport();
			viewport[0] = (int) imgTag.getOffset().x;
			viewport[1] = (int) imgTag.getOffset().y;
			
			//viewport[2] = (int) (viewportOrig.z * imgTag.getScale());
			//viewport[3] = (int) (viewportOrig.w * imgTag.getScale());
			
			
			if (Input.isPressed(Keyboard.KEY_C)) {
				imgTag.setCentered(!imgTag.isCentered());
			}
			
			if (Input.isPressed(Keyboard.KEY_HOME)) {
				if (type) {
					imgTag.setField(typedText);
				} else {
					type = true;
					typedText = "";
				}
			}
			
			UI.drawString(
					"[C]entered: " + imgTag.isCentered(), 2, 450);
		}
		
		if (type) {
			typedText += Input.getTypedKey();
			UI.drawString("Typed: "+typedText, 500, 720/2, false);
		}
	}

	private void shiftTag(float x, float y, float z) {
		if (heldTimer == 0f || heldTimer > 1f) {
			float s = meshTag.getScale();
			meshTag.getOffset().add(x*s, y*s, z*s);
		}
		heldTimer += Window.deltaTime;
	}

	public void update() {
		if (handler == null) 
			return;
		
		for(int i = 0; i < ViewModelHandler.MAX_MODELS; i++) {
			ViewModel vModel = handler.getDrawnModel(i);
			
			if (vModel == null) {
				continue;
			}
			
			meshData = vModel.getTexturedModel().getModel().getMeshData();
			
			if (meshData == MeshData.DEFUALT_DATA) {
				continue;
			}
			
			if (meshTagId >= meshData.getStructs().size()) {
				meshTagId = 0;
			}
			
			MeshTag tag = meshData.getStructs().get(meshTagId);
			if (tag == null) {
				continue;
			}
			
			meshTag = tag;
			
			if (tag instanceof ImageTag) {
				viewportOrig = new Vector4f();
				viewportOrig.x = ((ImageTag)tag).getViewport()[0];
				viewportOrig.y = ((ImageTag)tag).getViewport()[1];
				viewportOrig.z = ((ImageTag)tag).getViewport()[2];
				viewportOrig.w = ((ImageTag)tag).getViewport()[3];
			}
			
			break;
		}
		
		if (meshTag == null)
			return;
		
		editMeshTag();
	}

	public void toggle() {
		if (handler != null) {
			handler = null;
		} else if (Application.scene instanceof PlayableScene) {
			PlayableScene scene = (PlayableScene)Application.scene;
			
			handler = scene.getViewModelHandler();
		}
		
	}
}
