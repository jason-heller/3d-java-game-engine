package scene.mapscene.item;

import audio.Source;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;

public abstract class Item {

	protected boolean holdKeyToInteract;
	protected ViewModel viewModel;
	protected MapScene scene;
	protected Source source;
	
	public Item(MapScene scene) {
		this.scene = scene;
		viewModel = setViewModel();
		holdKeyToInteract = setHoldKeyToInteract();
	}
	
	/** Called in the constructor, abstract method in case i forget to set it
	 * @return The ViewModel this item will use
	 */
	public abstract ViewModel setViewModel();
	/** Called in the constructor, abstract method in case i forget to set it
	 * @return true if interact() should be called every frame the interact key is held, false otherwise
	 */
	public abstract boolean setHoldKeyToInteract();
	public abstract void interact();
	public abstract void interactEnd();
	public abstract void equip();
	public abstract void unequip();
	public abstract void update();
	
	public boolean isHoldKeyToInteract() {
		return holdKeyToInteract;
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}

	public void setSource(Source source) {
		this.source = source;
	}
}
