package scene.entity.hostile;

import java.util.List;
import java.util.SplittableRandom;

import org.joml.Vector3f;

import audio.AudioHandler;
import audio.Source;
import core.Application;
import dev.Debug;
import dev.cmd.Console;
import geom.AxisAlignedBBox;
import gl.Window;
import gl.entity.EntityRender;
import map.architecture.Architecture;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcRoom;
import map.architecture.components.GhostPoi;
import map.architecture.util.BspRaycast;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.Spawnable;
import scene.entity.object.SolidPhysProp;
import scene.entity.object.TripodCameraEntity;
import scene.entity.util.NavigableEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.PlayerHandler;
import scene.mapscene.MapScene;

public class GhostEntity extends NavigableEntity implements Spawnable {
	private PlayerEntity player;
	
	private float movementTimer = 0f, lookTimer = 0f;
	private float nextMoveTime = 0f;
	private float actionTimer = ACTION_INTERVAL;
	
	private static final float ACTION_INTERVAL = 5f;
	
	private int failedLookChecks = 0;
	
	private boolean corporeal = false;
	
	public float aggression = 0f;
	private GhostState state = GhostState.WANDERING;

	private static final float BBOX_WIDTH = 2f;
	private static final float BBOX_HEIGHT = 6f;

	private static final int MAX_FAILED_CHECKS = 4 * 20;
	
	private int actionsSinceLastAttack = 0;
	
	private Source source;
	
	public GhostEntity() {
		super("ghost", new Vector3f(BBOX_WIDTH, BBOX_HEIGHT, BBOX_WIDTH));
		this.setModel(EntityRender.billboard);
		//this.setTexture("default");
		//this.setTextureUnique("entity01_test", "entity/cute_lad.png");
		this.setTextureUnique("entity01_test", "entity/clown.png");
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
		this.setTextureUnique("entity01_test", "entity/clown.png");
		scale = 5;
		speed = 80;
		source = AudioHandler.checkoutSource();
		source.setAttenuation(.1f, 1f);
	}

	@Override
	public void update(PlayableScene scene) {
		if (state == GhostState.FROZEN) {
			return;
		}
		//UI.drawString((int)aggression + ", " + (int)(nextActionTime-this.actionTimer), 1f, true, this.getMatrix()).markAsTemporary();
		super.update(scene);
		
		source.setPosition(pos);
		
		this.visible = corporeal ? (System.currentTimeMillis() % 2000 < 1000) : false;
		this.solid = corporeal;
		
		Architecture arc = scene.getArchitecture();
		
		if (actionTimer > 0) {
			actionTimer -= Window.deltaTime;
			
			if (actionTimer <= 0) {
				doAction();
			}
		}

		if (state == GhostState.ATTACK) {
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
					
					if (failedLookChecks > MAX_FAILED_CHECKS && movementTimer > 40) {
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
			
			if (movementTimer > nextMoveTime) {
				if (Math.random() < (aggression + .4f)*.08f && actionsSinceLastAttack > 0) {
					startAttack();
				} else {
					nextMoveTime = 15f + ((float)Math.random() * 30f);
					movementTimer = 0f;
					changeTarget();
				}
				
				actionsSinceLastAttack++;
			}
		}
		movementTimer += Window.deltaTime;
	}
	
	public void doAction() {
		if (state == GhostState.PRE_ATTACK) {
			state = GhostState.ATTACK;

			navTarget.set(player.pos);
			return;
		}
		
		if (state == GhostState.ATTACK) {
			return;
		}
		
		SplittableRandom r = new SplittableRandom();
		Architecture arc = ((MapScene)Application.scene).getArchitecture();


		AxisAlignedBBox box = new AxisAlignedBBox(pos, new Vector3f(75,75,75));
		List<BspLeaf> leaves = arc.getVisibleLeavesIntersecting(box);
		List<Entity> entities = EntityHandler.getEntities(leaves);
		
		for(BspLeaf leaf : arc.getCluster(this.leaf)) {
			List<Entity> ents = EntityHandler.getEntities(leaf);
			if (ents != null) {
				entities.addAll(ents);
			}
		}
		
		if (entities.size() > 0) {
			int randEnt = r.nextInt(entities.size());
			
			Entity entity = entities.get(randEnt);
			
			if (entity == this) return;
			
			if (entity instanceof PlayerEntity) {
				int event = r.nextInt(4);
				switch(event) {
				case 0:	// idk spook
					EntityHandler.addEntity(new ApparitionEntity(model, texture, pos, rot, scale));
					break;
				default:
					source.play("ghost_voice");
				}
			}
			else if (entity instanceof SolidPhysProp) {
				SolidPhysProp physEnt = (SolidPhysProp)entity;

				float rx = ((float)r.nextDouble()-0.5f)*2f;
				float rz = ((float)r.nextDouble()-0.5f)*2f;
				Vector3f randDir = new Vector3f(rx, 5f, rz);
				physEnt.accelerate(randDir, 900f);
				physEnt.ghostInteraction();
			} else {
				if (entity instanceof TripodCameraEntity) {
					TripodCameraEntity tripod = (TripodCameraEntity)entity;
					tripod.breakTripod();
				}
			}
		}
		
		actionTimer = ACTION_INTERVAL + (float)r.nextDouble(ACTION_INTERVAL/2f);
	}

	public boolean raycastToPlayer(Architecture arc) {
		Vector3f to = Vector3f.sub(player.pos, pos);
		float toLen = to.length();
		to.div(toLen);
		BspRaycast ray = arc.raycast(pos, to);
		float dist = ray == null ? Float.POSITIVE_INFINITY : ray.getDistance();
		//LineRender.drawLine(pos, Vector3f.add(pos, Vector3f.mul(to, dist)));
		return (dist >= toLen + 1);
	}

	public void startAttack() {
		// nextActionTime = 120;
		state = GhostState.PRE_ATTACK;
		corporeal = true;
		Console.log("Time 2 kill");
		actionTimer = 3f;
		PlayerHandler.setThreatened(true);
	}
	
	public void endAttack() {
		failedLookChecks = 0;
		lookTimer = 0f;
		corporeal = false;
		PlayerHandler.setThreatened(false);
		state = GhostState.WANDERING;
		Console.log("Time 2 vibe");
		actionsSinceLastAttack = 0;
		aggression = (int)(aggression / 2.5f);
	}
	
	public boolean isAttacking() {
		return (state == GhostState.ATTACK || state == GhostState.PRE_ATTACK);
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
		return true;
	}

	public GhostState getState() {
		return state;
	}
	
	public void setState(GhostState state) {
		this.state = state;
	}
}
