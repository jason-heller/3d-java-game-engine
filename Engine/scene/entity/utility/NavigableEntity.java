package scene.entity.utility;

import java.util.Stack;

import org.joml.Vector3f;

import dev.Debug;
import gl.Window;
import gl.line.LineRender;
import map.architecture.components.ArcNavigation;
import scene.PlayableScene;
import util.Colors;

public abstract class NavigableEntity extends PhysicsEntity {
	
	public static final int IDLE = 0, PURSUE = 1, SEARCH = 2, WANDER = 3;

	private Vector3f navTarget = null;

	private int navTargetNode = -1;
	private Vector3f navStep = null;
	private ArcNavigation navigation = null;

	private Stack<Integer> path;
	private int pathIndex;
	
	protected float thinkInterval = .5f;
	private float thinkTimer = 0f;
	private boolean targetReachable = true;
	private int stateChangeTimer = 0;
	
	private Vector3f lastTargetPoint = new Vector3f();
	
	protected int moveState = PURSUE;
	
	protected float speed = 25f;
	
	public NavigableEntity(String name, Vector3f bounds) {
		super(name, bounds);
		path = new Stack<>();
		pathIndex = 0; // negative IDs in path => broadphase id, else narrowphase ID
		
	}
	
	public void initNavigation(ArcNavigation navigation) {
		this.navigation = navigation;
	}

	public void setTarget(Vector3f navTarget) {
		this.navTarget = navTarget;
		if (navTarget == null) {
			navTargetNode = -1;
			return;
		}

		think();

	}

	/** Controls entity movement planning. Happens every thinkInterval.
	 * 
	 */
	public void think() {
		thinkTimer = 0f;
		if (navTarget != null/* && moveState == PURSUE*/) {

			if (navStep != null && Vector3f.distanceSquared(pos, navTarget) < Vector3f.distanceSquared(pos, navStep)) {
				navStep = new Vector3f(navTarget);
			}
			
			int nearestNavNode = navigation.getNearest(navTarget);
			
			if (navTargetNode == nearestNavNode) {
				return;
			}
			
			navTargetNode = nearestNavNode;
			
			boolean successfulNav = navigation.navigateTo(this, navTarget);
			
			if (path.size() == 0 || !successfulNav) {
				pathIndex = -1;
				targetReachable = false;
				// moveState = SEARCH;
				stateChangeTimer = 0;
				navStep = new Vector3f(navTarget);
			} else {
				pathIndex = path.size() - 1;
				int nextIndex = path.get(pathIndex);
				navStep = new Vector3f(navigation.getNode(nextIndex).getPosition());
				
				targetReachable = true;
				moveState = PURSUE;
				lastTargetPoint.set(navTarget);
			}
		}
		/*
		else if (moveState == SEARCH) {
			stateChangeTimer++;
			
			if (stateChangeTimer == 10) {
				moveState = WANDER;
				stateChangeTimer = 0;
			}
			// Try to find player by wandering (but biased towards player's current position)
			boolean successfulNav = navigation.aStar(this, lastTargetPoint);
			pathIndex = path.size() - 1;
			
			if (pathIndex == -1 || !successfulNav) {
				targetReachable = false;
				moveState = WANDER;
				stateChangeTimer = 0;
			} else {
				navStep = new Vector3f(navigation.getNode(path.get(pathIndex)).getPosition());
				targetReachable = true;
			}
		}
		
		if (moveState == WANDER) {
			stateChangeTimer++;
			// Astar to random places on map
			if (stateChangeTimer == 15 || navStep == null) {
				boolean successfulNav = navigation.aStar(this, null);
				pathIndex = path.size() - 1;
				
				if (pathIndex == -1 || !successfulNav) {
					targetReachable = false;
				} else {
					navStep = new Vector3f(navigation.getNode(path.get(pathIndex)).getPosition());
					targetReachable = true;
				}
				
				stateChangeTimer = 0;
			}
		}*/
	}

	public Vector3f getTarget() {
		return navTarget;
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		thinkTimer += Window.deltaTime;
		
		// TODO: Check entity->player line of sight, if there is some, change state to pursue
		
		if (thinkTimer >= thinkInterval) {
			think();
		}
		
		if (navTarget != null) {
			
			if (navStep != null) {
				
				//float distSqrToTarget = Vector3f.distanceSquared(navTarget, pos);
				
				/*if (Vector3f.distanceSquared(navStep, pos) > distSqrToTarget) {
					navStep.set(navTarget);
				}*.
				
				// Reached target
				/*if (distSqrToTarget < 15f) {
					navStep = null;
					// navTarget = null;
					return;
				}*/
				
				Vector3f dir = Vector3f.sub(navStep, pos);
				dir.set(dir.x, 0f, dir.z).normalize();
				this.accelerate(dir, speed);
				
				if (Vector3f.distanceSquared(navStep, pos) < this.speed*2f) {
					pathIndex--;
					if (pathIndex < 0) {
						navStep = new Vector3f(navTarget);
					} else {
						int id = path.get(pathIndex);
						navStep = new Vector3f(navigation.getNode(id).getPosition());
					}
				}
			}
		}
		
		if (Debug.viewNavPath) {
			for(int i = 1; i < path.size(); i++) {
				Vector3f p1 = navigation.getNode(path.get(i)).getPosition();
				Vector3f p2 = navigation.getNode(path.get(i-1)).getPosition();
				
				LineRender.drawLine(p1, p2, Colors.RED);
			}
			
			if (navStep != null) {
				LineRender.drawLine(this.navStep, Vector3f.add(new Vector3f(0,5,0), this.navStep), Colors.GOLD);
			}
		}
	}

	public Stack<Integer> getPath() {
		return path;
	}
}
