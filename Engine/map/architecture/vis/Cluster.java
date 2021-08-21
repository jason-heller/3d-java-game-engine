package map.architecture.vis;

import gl.res.Model;

public class Cluster {
	
	private int diffuseId, bumpMapId;
	private Model model;

	// NOTE: We leave bumpMapId = 0 if there is none, since no bump map will use
	// that index anyways and since we use it as a flag for the shader (0 = no
	// bumpmap, 1+ = has a bumpmap)

	public Cluster(Model model, int diffuseId) {
		this.model = model;
		this.diffuseId = diffuseId;
	}

	public Model getModel() {
		return model;
	}
	
	public int getDiffuseId() {
		return diffuseId;
	}

	public int getBumpMapId() {
		return bumpMapId;
	}

	public void setBumpMapId(int bumpMapId) {
		this.bumpMapId = bumpMapId;
	}

}
