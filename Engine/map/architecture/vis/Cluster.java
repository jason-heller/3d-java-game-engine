package map.architecture.vis;

import gl.res.Model;

public class Cluster {
	
	private int diffuseId;
	private int bumpMapId = -1;
	private int specMapId = -1;
	private Model model;

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

	public int getSpecMapId() {
		return specMapId;
	}
	
	public void setSpecMapId(int specMapId) {
		this.specMapId = specMapId;
	}

}
