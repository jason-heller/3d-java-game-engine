package scene.mapscene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import audio.Source;
import core.Application;
import io.Input;
import scene.mapscene.item.CameraItem;
import scene.mapscene.item.Item;
import scene.mapscene.item.SpiritBoxItem;
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
		
		if (Input.isPressed(Keyboard.KEY_1)) {
			if (getCurrentItem() != getItem(0)) {
				unequipItem();
				equipItem(0);
			}
		}
		
		if (Input.isPressed(Keyboard.KEY_2)) {
			if (getCurrentItem() != getItem(1)) {
				unequipItem();
				equipItem(1);
			}
		}
		
		if (Input.isPressed("interact")) {
			onInteractPress();
		}
		
		if (Input.isDown("interact")) {
			onInteractHeld();
		}
		
		if (Input.isReleased("interact")) {
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
