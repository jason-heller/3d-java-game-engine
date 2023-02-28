package scene.entity.util;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import audio.AudioHandler;
import dev.cmd.Console;
import gl.Camera;
import gl.CameraFollowable;
import gl.Window;
import io.Controls;
import io.Input;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.goal.GoalEntity;
import ui.UI;
import util.Colors;

public class MapOverviewEntity extends Entity {
	
	private List<String> goalStrings;
	private List<GoalEntity> goals;
	
	private float timer = 0f;
	private float angle = 0f;
	
	private PlayerEntity player;
	private CameraFollowable focus;
	
	private boolean showContinue = false;
	
	public MapOverviewEntity(PlayableScene scene, PlayerEntity player, Bsp bsp) {
		super("recap");
		this.player = player;
		scene.getCamera().setControlStyle(Camera.NO_CONTROL);
		focus = scene.getCamera().getFocus();
		scene.getCamera().setFocus(null);
		
		PlayerEntity.enabled = false;
		List<Entity> allZeroedEntities = EntityHandler.getEntitiesByName("goal");
		
		goals = new ArrayList<>();
		goalStrings = new ArrayList<>();
		
		if (allZeroedEntities == null)
			return;
		
		for(Entity entity : allZeroedEntities) {
			if (entity instanceof GoalEntity) {
				goals.add((GoalEntity)entity);
				Console.log(entity.getName());
			}
		}
	}
	
	@Override
	public void update(PlayableScene scene) {
		angle += Window.deltaTime * .2f;
		timer += Window.deltaTime * 1.2f;
		
		float dx = (float)Math.sin(angle) * 20f;
		float dz = (float)Math.cos(angle) * 20f;
		
		Vector3f camPos = scene.getCamera().getPosition();
		camPos.set(player.getPosition());
		camPos.add(dx, 15f, dz);
		scene.getCamera().setYaw(-angle);
		scene.getCamera().setPitch(0.7853975f);
		
		UI.drawRect(128, 128, 1024, 464, Colors.DK_GREY).setOpacity(.75f);
		
		int len = goalStrings.size();
		if (timer > len) {
			if (len >= goals.size()) {
				if (!showContinue) {
					AudioHandler.play("click");
					showContinue = true;
				}
				
				UI.drawString("PRESS " + Input.getKeyName(Controls.get("tr_grind")) + " TO CONTINUE", 640, 500, .4f, true).setOpacity((timer - len) * 2f);
			} else {
				goalStrings.add(goals.get(len).toString());
				AudioHandler.play("click");
			}
		}
		
		for(int i = 0; i < goals.size(); i++) {
			if (i > timer)
				break;
			
			GoalEntity goal = goals.get(i);
			UI.drawString(goal.toString(), 200, 200 + (i*30)).setOpacity((timer - i) * 2f);
		}
		
		if (Input.isPressed("tr_grind")) {
			scene.getCamera().setFocus(focus);
			scene.getCamera().setControlStyle(Camera.THIRD_PERSON);
			EntityHandler.removeEntity(this);
			PlayerEntity.enabled = true;
		}
	}

}
