package scene.viewmodel;

import org.joml.Matrix4f;

import gl.res.mesh.ImageTag;

public class ViewModelEmf extends ViewModel {

	private static final float EMF_LIGHT_X = .25f;
	private static final float EMF_LIGHT_WIDTH = ((134f / 256f) - EMF_LIGHT_X) / 5f;
	
	private static final int EMF_VIEWPORT_WIDTH = 199 / 5;
	
	private int numEmfLights = 0;

	public ViewModelEmf() {
		super("emf", "default", new Matrix4f());
	}
	
	@Override
	public void equip() {
	}
	
	@Override
	public void holster() {
	}

	@Override
	public void update() {
		ImageTag img = (ImageTag) getTag(0);
		img.getUvOffsets()[2] = EMF_LIGHT_X + (EMF_LIGHT_WIDTH * numEmfLights);
		img.getViewport()[2] = EMF_VIEWPORT_WIDTH * numEmfLights; 
		
	}

	public void setNumEmfLights(int numEmfLights) {
		this.numEmfLights = numEmfLights;
	}
}
