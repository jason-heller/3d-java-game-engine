package scene.entity.goal;

import gl.Window;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import ui.UI;

public class GoalCompleteEntity extends Entity {
	
	private String text;
	private float timer;
	private float scale = .5f;
	
	private int posIndex = 0;
	
	private static final float FADE_START_SEC = 4;
	private static final float FADE_DURATION_SEC = 1;
	
	private static boolean[] goalCompletePositions = new boolean[] {true, true, true, true};		// There should never be more than 4 goals per run
	
	public GoalCompleteEntity(String text) {
		super("goal complete");
		this.text = text;
		
		for(int i = 0; i < goalCompletePositions.length; i++) {
			if (goalCompletePositions[i]) {
				posIndex = i;
				goalCompletePositions[i] = false;
				break;
			}
		}
	}
	
	@Override
	public void update(PlayableScene scene) {
		timer += Window.deltaTime;
		
		if (timer > FADE_START_SEC) {
			float t = (timer - FADE_START_SEC);
			scale = (1f - (t / FADE_DURATION_SEC)) * 0.5f;
			
			if (t > FADE_DURATION_SEC) {
				EntityHandler.removeEntity(this);
				goalCompletePositions[posIndex] = true;
			}
		}
		
		UI.drawString(text, 640, 170 + (posIndex * 30), scale, true);
	}
}
