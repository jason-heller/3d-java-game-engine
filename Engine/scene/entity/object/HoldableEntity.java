package scene.entity.object;

import org.joml.Vector3f;

import dev.cmd.Console;
import geom.CollideUtils;
import gl.Window;
import io.Input;
import map.architecture.Architecture;
import scene.PlayableScene;
import scene.entity.util.PhysicsEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;

public abstract class HoldableEntity extends PhysicsEntity {
	
	private boolean held;

	protected float emfLevel = 0f;
	
	public HoldableEntity() {
		super("", new Vector3f());
		solid = true;
	}
	
	public HoldableEntity(String name, Vector3f bounds) {
		super(name, bounds);
		solid = true;
	}
	
	public void hold() {
		held = true;
		PlayerHandler.holding = this;
	}
	
	public void release() {
		held = false;
		PlayerHandler.holding = null;
	}
	
	public boolean isHeld() {
		return held;
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		
		PlayerEntity player = scene.getPlayer();
		Vector3f camVec = scene.getCamera().getDirectionVector();
		
		if (Input.isPressed("interact")) {
			if (held) {
				release();
			} else {
				float ray = bbox.collide(player.pos, camVec);
				Console.log(ray);
				if (!Float.isInfinite(ray) && ray <= 20f) {
					//if (PlayerHandler.holding == null || Vector3f.distanceSquared(PlayerHandler.holding.pos, player.pos) < ray * ray ) {
						
						if (PlayerHandler.holding != null)
							PlayerHandler.holding.release();
				
						hold();
					//}
				}
			}
		}
		
		applyGravity = !held;
		
		if (held) {
			
			if (Vector3f.distanceSquared(player.pos, pos) > 800f) {
				release();
				return;
			}
			
			Vector3f targetPos = Vector3f.add(player.pos, Vector3f.mul(camVec, 10f + bbox.getBounds().x));
			targetPos.y += player.getBBox().getHeight() / 2f;
			Vector3f move = Vector3f.sub(targetPos, pos).mul(15f);
			
			// Sweep
			float len = move.length();
			Vector3f dir = new Vector3f(move).div(len);
			Architecture arc = scene.getArchitecture();
			float ray = CollideUtils.raycast(arc.getActiveLeaves().getNear(), arc.bsp, pos, dir);
			
			if (ray < len * Window.deltaTime * 2f) {
				vel.zero();
			} else {
				vel.set(move);
			}
		}
	}

	public void ghostInteraction() {
		emfLevel = 3;
	}
	
	public float getEmfLevel() {
		return emfLevel;
	}
}
