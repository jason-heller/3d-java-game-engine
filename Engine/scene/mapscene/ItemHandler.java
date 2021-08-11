package scene.mapscene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import audio.Source;
import core.Application;
import dev.Console;
import io.Input;
import scene.mapscene.item.CameraItem;
import scene.mapscene.item.Item;
import scene.mapscene.item.MotionSensorItem;
import scene.mapscene.item.PhysPropSpawnerItem;
import scene.mapscene.item.SpiritBoxItem;
import scene.mapscene.item.TripodCameraItem;
import scene.viewmodel.ViewModelHandler;

public class ItemHandler {

	private ViewModelHandler viewModelHandler;
	
	private List<Item> items = new ArrayList<>();
	private Item currentItem;
	
	private Source source;
	
	public ItemHandler(MapScene scene, ViewModelHandler viewModelHandler) {
		this.viewModelHandler = viewModelHandler;

		source = AudioHandler.checkoutSource();
		source.setAttenuation(1, 1);

		addItem(new SpiritBoxItem(scene));
		addItem(new CameraItem(scene));
		addItem(new MotionSensorItem(scene));
		addItem(new TripodCameraItem(scene));
		addItem(new PhysPropSpawnerItem(scene));
		currentItem = null;
	}
	
	public void onInteractPress() {
		if (currentItem == null)
			return;
		
		currentItem.interact();
	}
	
	public void onInteractHeld() {
		if (currentItem == null)
			return;
		
		if (currentItem.isHoldKeyToInteract()) {
			currentItem.interact();
		}
	}
	
	public void onInteractRelease() {
		if (currentItem == null)
			return;
		
		currentItem.interactEnd();
	}
	
	public void update() {
		if (Console.isVisible()) return;
		for(int i = 0; i < 5; i++) {
			if (Input.isPressed(Keyboard.KEY_1 + i)) {
				if (getCurrentItem() != getItem(i)) {
					unequipItem();
					equipItem(i);
				}
			}
		}
		
		if (Input.isPressed(Input.KEY_LMB)) {
			onInteractPress();
		}
		
		if (Input.isDown(Input.KEY_LMB)) {
			onInteractHeld();
		}
		
		if (Input.isReleased(Input.KEY_LMB)) {
			onInteractRelease();
		}
		
		if (currentItem != null) {
			currentItem.update();
		}
		
		source.setPosition(Application.scene.getCamera().getPosition());
	}
	
	public void equipItem(int index) {
		currentItem = items.get(index);
		viewModelHandler.setDrawnModel(0, currentItem.getViewModel());
		currentItem.equip();
		currentItem.getViewModel().equip();
	}
	
	public void unequipItem() {
		if (currentItem == null) return;
		currentItem.unequip();
		currentItem.getViewModel().holster();
		viewModelHandler.removeDrawnModel(0);
		source.stop();
		currentItem = null;
		
	}
	
	public Item getCurrentItem() {
		return currentItem;
	}

	private void addItem(Item item) {
		items.add(item);
		item.setSource(this.source);
	}

	public Item getItem(int i) {
		return items.get(i);
	}

}
