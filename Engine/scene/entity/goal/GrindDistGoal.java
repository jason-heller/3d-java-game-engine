package scene.entity.goal;

import scene.entity.util.PlayerEntity;
import scene.mapscene.MapScene;

public class GrindDistGoal extends GoalEntity {

	private float dist;

	public GrindDistGoal(int difficulty, float dist) {
		super(difficulty);
		this.dist = dist;
	}

	@Override
	public void checkGoal(MapScene scene) {
		if (isCompleted() || MapScene.preRound)
			return;

		PlayerEntity player = scene.getPlayer();
		if (!player.isGrinding() && player.getGrindLen() / 12f >= dist) {
			completeGoal();
		}
	}

	@Override
	public String toString() {
		return "Grind " + dist + " meters";
	}
}
