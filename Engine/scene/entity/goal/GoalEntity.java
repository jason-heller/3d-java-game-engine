package scene.entity.goal;

import audio.AudioHandler;
import dev.cmd.Console;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.mapscene.MapScene;

public abstract class GoalEntity extends Entity {
	
	private GoalDifficulty difficulty;
	private boolean isCompleted;
	
	public GoalEntity(int difficulty) {
		super("goal");
		this.difficulty = GoalDifficulty.values()[difficulty];
	}
	
	public GoalDifficulty getDifficulty() {
		return difficulty;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public void update(PlayableScene scene) {
		if (isCompleted() || MapScene.preRound)
			return;
		
		MapScene mapScene = (MapScene)scene;
		
		checkGoal(mapScene);
	}
	
	public abstract void checkGoal(MapScene mapScene);
	
	protected void completeGoal() {
			
		Console.log("#* Goal completed: " + toString());
		EntityHandler.addEntity(new GoalCompleteEntity("#*" + toString()));
		AudioHandler.play("goal_completed");
		
		isCompleted = true;
	}
}
