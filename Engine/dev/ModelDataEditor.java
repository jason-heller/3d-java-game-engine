package dev;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import core.Application;
import dev.cmd.Console;
import gl.res.mesh.ImageTag;
import gl.res.mesh.MeshData;
import gl.res.mesh.MeshTag;
import gl.res.mesh.TextTag;
import io.Input;
import scene.PlayableScene;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import scene.viewmodel.ViewModelHandler;
import ui.UI;
import util.Colors;

public class ModelDataEditor {
	private ViewModelHandler handler;
	
	private MeshData meshData;
	
	private float grabX, grabY;
	private MeshTag grabbedItem = null, hoveredItem = null;
	
	private float offset = .1f;
	
	private boolean type = false;
	private String typedText = "";
	
	private boolean dragW = false, dragH = false;
	
	private void editMeshTag() {
		if (meshData == null) return;
		MapScene scene = (MapScene)Application.scene;
		scene.getViewModelHandler().clearSway();

		float mx = (Mouse.getX() - 280) / 360f;
		float my = (Mouse.getY() - 680) / 360f;

		
		hoveredItem = null;
		
		float tx = 0, ty = 0, tw = 0, th = 0;
		
		for(MeshTag tag : meshData.getTags()) {
			tx = tag.getOffset().x;
			ty = tag.getOffset().y;
			tw = tag.getWidth() / 720f;
			th = tag.getHeight() / 720f;
			if (mx > tx && my > ty && mx < tx + tw && my < ty + th) {
				hoveredItem = tag;
				break;
			}
		}
		
		if (Input.isPressed(Input.KEY_LMB) && hoveredItem != null) {
			grabbedItem = hoveredItem;
			grabX = mx - hoveredItem.getOffset().x;
			grabY = my - hoveredItem.getOffset().y;
			
			if (hoveredItem instanceof ImageTag) {
				if (mx > tx + tw-.05f) {
					dragW = true;
					grabX -= ((ImageTag)grabbedItem).getViewport()[2] / 720f;
				}
				if (my > ty + th-.05f) {
					dragH = true;
					grabY += ((ImageTag)grabbedItem).getViewport()[3] / 720f;
				}
			}
				
		}
		
		if (Input.isReleased(Input.KEY_LMB)) {
			grabbedItem = null;
			dragW = dragH = false;
		}
		
		if (grabbedItem != null) {
			hoveredItem = grabbedItem;
			if (dragW) {
				((ImageTag)grabbedItem).getViewport()[2] = (int)((mx - grabX) * 360f);
				Console.log("X", (int)((mx - grabX) * 360f));
			} else if (dragH) {
				((ImageTag)grabbedItem).getViewport()[3] = (int)((-my + grabY) * 360f);
				Console.log(my,grabY);
			} else {
				grabbedItem.getOffset().x = (mx - grabX);
				grabbedItem.getOffset().y = (my - grabY);
			}
		}
		
		if (hoveredItem != null) {
			int xPixel = (int) (hoveredItem.getOffset().x * 360f);
			int yPixel = (int) (-hoveredItem.getOffset().y * 360f);
			xPixel += 280f;
			Vector3f color = (hoveredItem == grabbedItem) ? Colors.RED : Colors.BLUE;
			int width = (int) hoveredItem.getWidth() / 2;
			int height = (int) hoveredItem.getHeight() / 2;
			UI.drawHollowRect(xPixel, yPixel, width,
					height, 1, color);

			if (mx > tx + tw-.05f) {
				int x = xPixel + width;
				int y = yPixel + height / 2;
				UI.drawLine(x, y, x + 12, y, 1, Colors.GREEN);
			}

			if (my > ty + th-.05f) {
				int x = xPixel + width / 2;
				int y = yPixel;
				UI.drawLine(x, y, x, y - 12, 1, Colors.GREEN);

			}
			

			UI.drawString("tag: " + hoveredItem.getClass().getSimpleName() + " (" + hoveredItem + ")" + "\n" +
					"snap: " + offset +
					"\npos: " + hoveredItem.getOffset() +
					"\nscale: " + hoveredItem.getScale(), 2, 350, .25f, false);
		}
		
		if (hoveredItem instanceof TextTag) {
			TextTag textTag = (TextTag)hoveredItem;
			
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
					"[C]entered: " + textTag.isCentered(), 2, 450, .25f, false);
		}
		
		if (hoveredItem instanceof ImageTag) {
			ImageTag imgTag = (ImageTag)hoveredItem;
			
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
					"[C]entered: " + imgTag.isCentered(), 2, 450, .25f, false);
		}
		
		if (type) {
			typedText += Input.getTypedKey();
			UI.drawString("Typed: "+typedText, 500, 720/2, false);
		}
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
			
			break;
		}
		
		editMeshTag();
	}

	public void toggle() {
		if (handler != null) {
			handler = null;
			Input.requestMouseGrab();
		} else if (Application.scene instanceof PlayableScene) {
			PlayableScene scene = (PlayableScene)Application.scene;
			
			handler = scene.getViewModelHandler();
			Debug.god = true;
			Debug.fullbright = true;
			Console.send("ghost_freeze");
			Input.requestMouseRelease();
		}
		
	}
}
