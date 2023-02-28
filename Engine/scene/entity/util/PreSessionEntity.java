package scene.entity.util;

import java.util.ArrayList;
import java.util.List;

import core.App;
import io.Controls;
import io.Input;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.goal.GoalEntity;
import scene.mapscene.MapScene;
import ui.Font;
import ui.UI;
import util.Colors;

public class PreSessionEntity extends Entity {
	
	private List<GoalEntity> goals;
	
	private static final float x = 0, y = 540, width = 300, height = 120;
	
	private PlayerEntity player;
	
	public PreSessionEntity(PlayerEntity player) {
		super("recap");
		this.player = player;
		
		List<Entity> allZeroedEntities = EntityHandler.getEntitiesByName("goal");
		
		goals = new ArrayList<>();
		
		if (allZeroedEntities == null)
			return;
		
		for(Entity entity : allZeroedEntities) {
			if (entity instanceof GoalEntity) {
				goals.add((GoalEntity)entity);
			}
		}
	}
	
	@Override
	public void update(PlayableScene scene) {
		UI.drawRect(x, y, width, height, Colors.BLACK).setOpacity(.75f);
		UI.drawString(Font.vhsFont, "Goals:", x + 3f, y + 3f, .25f, Integer.MAX_VALUE, false);
		UI.drawLine(x + 3f, y + 22f, x + 197, y + 22f, 1f, Colors.GREY);
		
		for(int i = 0; i < goals.size(); i++) {
			GoalEntity goal = goals.get(i);
			UI.drawString(Font.vhsFont, goal.toString(), x + 3f, y + 30f + (i*20), .15f, Integer.MAX_VALUE, false);
		}
		
		UI.drawString(Font.vhsFont, "#APRESS " + Input.getKeyName(Controls.get("start")) + "\nTO START SESH", 1280 - 250, 550, .3f, Integer.MAX_VALUE, false);
		
		if (Input.isPressed("start")) {
			player.setGrindLen(0f);
			EntityHandler.removeEntity(this);
			MapScene.preRound = false;
			((MapScene)App.scene).getMusicHandler().playNext();
		}
	}

}
