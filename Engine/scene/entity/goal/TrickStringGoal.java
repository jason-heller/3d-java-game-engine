package scene.entity.goal;

import scene.mapscene.MapScene;
import scene.mapscene.trick.Trick;
import scene.mapscene.trick.TrickList;
import scene.mapscene.trick.TrickManager;

public class TrickStringGoal extends GoalEntity {
	
	private Trick[] reqTricks;
	private String text;
	
	public TrickStringGoal(int difficulty, String trickStr) {
		super(difficulty);
		
		String[] trickStrParse = trickStr.split(",");
		reqTricks = new Trick[trickStrParse.length];
		
		text = "Land this combo: ";
		
		for(int i = 0; i < reqTricks.length; i++) {
			reqTricks[i] = TrickList.getTrick(trickStrParse[i]);
			text += trickStrParse[i] + ((i == reqTricks.length - 1) ? "" : " + ");
		}
	}
	
	@Override
	public void checkGoal(MapScene scene) {
		TrickManager tm = scene.getPlayer().getTrickManager();
		
		if (tm.comboContainsOrdered(reqTricks))
			completeGoal();
	}

	@Override
	public String toString() {
		return text;
	}
}
