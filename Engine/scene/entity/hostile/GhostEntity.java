package scene.entity.hostile;

import org.joml.Vector3f;

import audio.AudioHandler;
import audio.Source;
import core.Application;
import dev.Console;
import dev.Debug;
import gl.Window;
import gl.entity.EntityRender;
import gl.line.LineRender;
import map.architecture.Architecture;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcRoom;
import map.architecture.components.GhostPoi;
import map.architecture.util.BspRaycast;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import scene.entity.Spawnable;
import scene.entity.util.NavigableEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;
import ui.UI;

public class GhostEntity extends NavigableEntity implements Spawnable {
	private PlayerEntity player;
	
	private float actionTimer = 0f, lookTimer = 0f;
	private float nextActionTime = 0f;
	
	private int failedLookChecks = 0;
	
	private boolean corporeal = false;
	
	public float aggression = 0f;
	private boolean attacking = false;

	private static final float BBOX_WIDTH = 2f;
	private static final float BBOX_HEIGHT = 6f;

	private static final int MAX_FAILED_CHECKS = 4 * 20;
	
	private int actionsSinceLastAttack = 0;
	
	private Source source;
	
	public GhostEntity() {
		super("ghost", new Vector3f(BBOX_WIDTH, BBOX_HEIGHT, BBOX_WIDTH));
		this.setModel(EntityRender.billboard);
		//this.setTexture("default");
		this.setTextureUnique("entity01_test", "entity/cute_lad.png");
		scale = 5;
		speed = 80;
		source = AudioHandler.checkoutSource();
		source.setAttenuation(.1f, 1f);
	}
	
	public GhostEntity(PlayerEntity player, ArcNavigation nav) {
		super("ghost", new Vector3f(BBOX_WIDTH, BBOX_HEIGHT, BBOX_WIDTH));
		this.player = player;
		this.setNavigation(nav);
		this.setModel(EntityRender.billboard);
		//this.setTexture("default");
		this.setTextureUnique("entity01_test", "entity/cute_lad.png");
		scale = 5;
		speed = 80;
		source = AudioHandler.checkoutSource();
		source.setAttenuation(.1f, 1f);
	}

	@Override
	public void update(PlayableScene scene) {	
		//UI.drawString((int)aggression + ", " + (int)(nextActionTime-this.actionTimer), 1f, true, this.getMatrix()).markAsTemporary();
		super.update(scene);
		
		LineRender.drawLine(pos, scene.getPlayer().pos);
		
		source.setPosition(pos);
		
		this.visible = corporeal ? (System.currentTimeMillis() % 2000 < 1000) : false;
		this.solid = corporeal;
		
		Architecture arc = scene.getArchitecture();
		if (attacking) {
			// Try to find player and kill them
			lookTimer += Window.deltaTime;
			
			// Periodically raycast towards the player, if no walls are hit, head directly towards player
			if (lookTimer > .25f) {
				lookTimer = 0f;
				boolean canSeePlayer = raycastToPlayer(arc);
				
				if (canSeePlayer) {
					// We can see the player
					navTarget.set(player.pos);
					failedLookChecks = 0;
					
				} else {
					// Raycast didn't reach player, treat it as not seeing them
					failedLookChecks++;
					
					if (failedLookChecks > MAX_FAILED_CHECKS && actionTimer > 40) {
						endAttack();
					}
				}
				
				if (Vector3f.distanceSquared(pos, navTarget) < 25) {
					changeTarget();
				}
			}
			
			// If target is reached (meaning player wasn't found) choose a random room to search
			
			if (player.getBBox().intersects(bbox) && PlayerEntity.getHp() > 0 && !Debug.god) {
				player.takeDamage(15);
				AudioHandler.play("fall");
			}
		} else {
			if (actionTimer > nextActionTime) {
				// TODO: Attacks should be more based on anger
				if (Math.random() < aggression*.08 && actionsSinceLastAttack > 0) {
					startAttack();
				} else {
					nextActionTime = 15f + ((float)Math.random() * 30f);
					actionTimer = 0f;
					changeTarget();
					if (scene.getArchitecture().isLeafAudible(this.getLeaf())) {
						source.play("ghost_voice");
					}
				}
				
				actionsSinceLastAttack++;
			}
		}
		actionTimer += Window.deltaTime;
	}
	
	public boolean raycastToPlayer(Architecture arc) {
		Vector3f to = Vector3f.sub(player.pos, pos);
		float toLen = to.length();
		to.div(toLen);
		BspRaycast ray = arc.raycast(pos, to);
		float dist = ray == null ? Float.POSITIVE_INFINITY : ray.getDistance();
		
		return (dist >= toLen + 1);
	}

	private void startAttack() {
		// nextActionTime = 120;
		attacking = true;
		corporeal = true;
		navTarget.set(player.pos);
		PlayerHandler.setThreatened(true);
		Console.log("Time 2 kill");
	}
	
	private void endAttack() {
		failedLookChecks = 0;
		lookTimer = 0f;
		corporeal = false;
		PlayerHandler.setThreatened(false);
		attacking = false;
		Console.log("Time 2 vibe");
		actionsSinceLastAttack = 0;
		aggression = (int)(aggression / 2.5f);
	}

	public void changeTarget() {
		Vector3f poiPos = findNextPoi().getPosition();
		this.navTarget.set(poiPos.x, poiPos.y + bbox.getHeight(), poiPos.z);
	}
	
	public GhostPoi findNextPoi() {
		Bsp bsp = ((PlayableScene)Application.scene).getArchitecture().bsp;
		int roomId = 1 + (int)(Math.random() * (bsp.rooms.length - 1));
		ArcRoom room = bsp.rooms[roomId];
		int poiId = (int)(Math.random() * room.getGhostPois().length);
		return room.getGhostPois()[poiId];
	}

	@Override
	public boolean spawn(Vector3f pos, Vector3f rot, String... args) {
		PlayableScene scene = (PlayableScene)Application.scene;
		ArcNavigation navigation = scene.getArchitecture().getNavigation();
		
		player = scene.getPlayer();
		this.pos.set(pos);
		setNavigation(navigation);
		changeTarget();
		//setTarget(scene.getPlayer().pos);
		return true;
	}
}
