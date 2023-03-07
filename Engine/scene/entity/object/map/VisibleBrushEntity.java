package scene.entity.object.map;

import scene.PlayableScene;

public class VisibleBrushEntity extends BrushEntity {

	private int solidity;

	public VisibleBrushEntity(String name, int solidity) {
		super(name);
		this.solidity = solidity;
	}

	@Override
	public void update(PlayableScene scene) {
		super.update(scene);

	}
}
