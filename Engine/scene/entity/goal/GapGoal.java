package scene.entity.goal;

import scene.mapscene.MapScene;
import scene.mapscene.trick.Trick;
import scene.mapscene.trick.TrickList;
import scene.mapscene.trick.TrickManager;

public class GapGoal extends GoalEntity {
	
	private Trick[] reqTricks;
	private String[] gaps;
	
	private String text;
	
	private boolean[] completed;
	
	public GapGoal(int difficulty, String trickStr, String gaps, String text) {
		super(difficulty);
		
		String[] trickStrParse = trickStr.split(",");
		reqTricks = new Trick[trickStrParse.length];
		completed = new boolean[reqTricks.length];
		
		this.gaps = gaps.split(">");
		this.text = text;
		
		for(int i = 0; i < reqTricks.length; i++) {
			reqTricks[i] = TrickList.getTrick(trickStrParse[i]);
		}
	}
	
	@Override
	public void checkGoal(MapScene scene) {
		for(int i = 0; i < gaps.length; i++) {
			if (completed[i])
				continue;
				
			TrickManager tm = scene.getPlayer().getTrickManager();
			
			if (tm.comboContainsGap(gaps[i]) && tm.comboContains(reqTricks)) {
				completed[i] = true;
			}
		}
		
		checkForFullCompletion();
	}
	
	private void checkForFullCompletion() {
		for(int i = 0; i < completed.length; i++) {
			if (!completed[i])
				return;
		}
		
		this.completeGoal();
	}

	@Override
	public String toString() {
		return text;
	}
}
