package map.architecture.functions.commands;

import org.joml.Vector3f;

import core.Application;
import dev.cmd.Console;
import map.architecture.functions.ArcFunction;
import scene.PlayableScene;
import scene.entity.DummyEntity;
import scene.entity.EntityHandler;
import scene.entity.util.NavigableEntity;

public class PathNode extends ArcFunction {

	private int id, next, prev;
	private String cmd;
	
	public PathNode(Vector3f pos, int id, int next, int prev, String cmd) {
		super("pathnode " + id, pos);
		this.id = id;
		this.cmd = cmd;
		this.next = next;
		this.prev = prev;
	}

	@Override
	public void trigger(String[] args) {
		PlayableScene scene = (PlayableScene)Application.scene;
		int target = Integer.parseInt(args[1]);
		
		if (this.id == target) {
			NavigableEntity entity = (NavigableEntity)EntityHandler.getEntity(args[0]);
			
			if (args.length >= 3 && args[2].equals("next")) {
				entity.navPathNode = this.prev;
				entity.setTarget(null);
				Console.send(cmd);
			} else {
				entity.setTarget(this.pos);
			}
			
			entity.setNavigation(scene.getArchitecture().getNavigation());
		}
	}

}
