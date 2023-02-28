package scene.entity.goal;

import scene.mapscene.MapScene;

public class ScoreGoal extends GoalEntity {
	
	private int score;
	private boolean requireCombo;
	
	public ScoreGoal(int difficulty, int score, boolean requireCombo) {
		super(difficulty);
		this.score = score;
		this.requireCombo = requireCombo;
	}
	
	@Override
	public void checkGoal(MapScene scene) {
		int compareScore = 0;
		
		if (requireCombo) {
			compareScore = scene.getPlayer().getTrickManager().getHighestCombo();
		} else {
			compareScore = scene.getScore();
		}
		
		if (compareScore >= score) {
			completeGoal();
		}
	}
	
	@Override
	public String toString() {
		return "Get " + score + " points" + (requireCombo ? " in a combo" : "");
	}
}
