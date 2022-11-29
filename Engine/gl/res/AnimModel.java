package gl.res;

public class AnimModel {
	
	private Model[] models;

	public AnimModel(int numMeshes) {
		models = new Model[numMeshes];
	}

	public void addModel(int index, Model model) {
		this.models[index] = model;
	}
	
	public Model[] getModels() {
		return models;
	}

	public Model getModel(int i) {
		return models[i];
	}
}
