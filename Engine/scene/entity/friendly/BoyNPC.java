package scene.entity.friendly;

import org.joml.Vector3f;

import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.util.NavigableEntity;
import scene.entity.util.PlayerEntity;

public class BoyNPC extends NavigableEntity {
	private PlayerEntity player;
	
	private final float MOVE_SPEED = 100;
	private boolean reachedNode = false;
	
	public BoyNPC(Vector3f pos, int pathTarget, PlayerEntity player) {
		super("boy", new Vector3f(3f, 5f, 3f));
		this.pos.set(pos);
		this.navPathNode = pathTarget;
		this.player = player;
		this.setModel(EntityHandler.billboard);
		this.setTextureUnique("entity01_test", "entity/cute_lad.png");
		scale = 5;
		speed = MOVE_SPEED;
		
	}

	@Override
	public void update(PlayableScene scene) {
		if (this.navTarget != null && !Float.isNaN(this.navTarget.x)) {
			super.update(scene);
		}
		
		this.stepTimeActual = 0f;
		this.stepTimeEst = Float.POSITIVE_INFINITY;
		
		if (this.navTarget == null) {
			scene.getArcHandler().getArchitecture().callCommand(this, "navigate " + name + " " + this.navPathNode);
			reachedNode = false;
		} else {
			
			if (Vector3f.distanceSquared(pos, navTarget) < 100) {
				scene.getArcHandler().getArchitecture().callCommand(this, "navigate " + name + " " + this.navPathNode + " next");
				reachedNode = true;
			}
		}
		
		float dSqr = Vector3f.distanceSquared(pos, player.pos);
		if (dSqr > 2000 && reachedNode) {
			speed = 0;
		} else if (dSqr <= 2000) {
			speed = MOVE_SPEED;
		}
	}
	
}
