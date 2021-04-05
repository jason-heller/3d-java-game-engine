package scene.entity.util;

import java.util.Stack;

import org.joml.Vector3f;

import dev.Console;
import dev.Debug;
import gl.Window;
import gl.line.LineRender;
import map.architecture.components.ArcNavigation;
import scene.PlayableScene;
import util.Colors;

public abstract class NavigableEntity extends PhysicsEntity {
	
	public static final int IDLE = 0, PURSUE = 1, SEARCH = 2, WANDER = 3;

	protected Vector3f navTarget = null;

	private int navTargetNode = -1;
	private Vector3f navStep = null;
	private ArcNavigation navigation = null;

	private Stack<Integer> path;
	private int pathIndex;
	
	protected float thinkInterval = .5f;
	private float thinkTimer = 0f;
	private boolean targetReachable = true;
	private int stateChangeTimer = 0;
	
	private float stepTimeEst = Float.POSITIVE_INFINITY, stepTimeActual = 0f;
	
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

	protected void setTarget(Vector3f navTarget) {
		this.navTarget = navTarget;
		if (navTarget == null) {
			navTargetNode = -1;
			return;
		}

	}

	/** Controls entity movement planning. Happens every thinkInterval.
	 * 
	 */
	public void think() {
		thinkTimer = 0f;
		if (moveState == SEARCH) {
			stateChangeTimer++;
			
			if (stateChangeTimer == 10) {
				moveState = WANDER;
				stateChangeTimer = 0;
			}
			// Try to find player by wandering (but biased towards player's current position)
			boolean successfulNav = navigation.navigateTo(this, lastTargetPoint);
			pathIndex = path.size() - 1;
			
			if (pathIndex == -1 || !successfulNav) {
				targetReachable = false;
				moveState = WANDER;
				stateChangeTimer = 0;
			} else {
				//navStep = new Vector3f(navigation.getNode(path.get(pathIndex)).getPosition());
				targetReachable = true;
				
				setTarget(lastTargetPoint);
				calcNavStep();
			}
		}
		else if (moveState == WANDER) {
			stateChangeTimer++;
			// Astar to random places on map
			if (stateChangeTimer >= 20 || navTarget == null) {
				boolean successfulNav = navigation.navigateTo(this, null);
				pathIndex = path.size() - 1;
				
				if (pathIndex == -1 || !successfulNav) {
					targetReachable = false;
				} else {
					//navStep = new Vector3f(navigation.getNode(path.get(pathIndex)).getPosition());
					targetReachable = true;
					
					//setTarget(navigation.getNode(path.get(0)).getPosition());
					calcNavStep();
				}
				
				stateChangeTimer = 0;
			}
		}
		
		if (navTarget != null/* && moveState == PURSUE*/) {

			if (navStep != null && Vector3f.distanceSquared(pos, navTarget) < Vector3f.distanceSquared(pos, navStep)) {
				navStep = new Vector3f(navTarget);
				calcStepTime(navTarget);
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
				calcStepTime(navTarget);
			} else {
				// Path changed, adjust navStep to match
				/*
				int nextIndex = path.get(pathIndex);
				Vector3f newStep = new Vector3f(navigation.getNode(nextIndex).getPosition());
				if (navStep != null && newStep.sub(navStep).lengthSquared() > 2) {
					calcStepTime(newStep);
				}
				navStep = newStep;
				//setTarget(lastTargetPoint);*/
				
				pathIndex = path.size() - 1;
				int nextIndex = path.get(pathIndex);
				Vector3f newStep = new Vector3f(navigation.getNode(nextIndex).getPosition());
				if (navStep != null && newStep.sub(navStep).lengthSquared() > 2) {
					calcStepTime(newStep);
				}
				navStep = newStep;
				
				targetReachable = true;
				moveState = PURSUE;
				lastTargetPoint.set(navTarget);
			}
			
			/*
				pathIndex = path.size() - 1;
				int nextIndex = path.get(pathIndex);
				navStep = new Vector3f(navigation.getNode(nextIndex).getPosition());
				
				targetReachable = true;
				moveState = PURSUE;
				lastTargetPoint.set(navTarget);
			}*/
		}
	}

	public Vector3f getTarget() {
		return navTarget;
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		thinkTimer += Window.deltaTime;
		if (thinkTimer >= thinkInterval) {
			think();
		}

		stepTimeActual += Window.deltaTime;
		if (stepTimeActual > stepTimeEst) {
			if (Vector3f.sub(pos, navTarget).lengthSquared() > 40000) {
				pos.set(navStep);
				moveToNextNode();
			} else {
				//Vector3f interp = Vector3f.sub(navStep, pos).normalize().mul(Window.deltaTime * speed);
				//pos.add(interp);
				if (grounded) {
					this.jump(30f);
				}
			}
		}
		
		// TODO: Check entity->player line of sight, if there is some, change state to pursue
		
		
		
		if (navTarget != null) {
			
			if (navStep != null) {
				
				Vector3f dir = Vector3f.sub(navStep, pos);
				dir.set(dir.x, 0f, dir.z).normalize();
				this.accelerate(dir, speed);
				
				
				
				if (Vector3f.distanceSquared(navStep, pos) < this.speed*2f) {
					moveToNextNode();
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

	private void moveToNextNode() {
		pathIndex--;
		if (pathIndex < 0) {
			navStep = new Vector3f(navTarget);
			calcStepTime(navTarget);
		} else {
			calcNavStep();
		}
	}

	private void calcNavStep() {
		int id = path.get(pathIndex);
		Vector3f newStep = new Vector3f(navigation.getNode(id).getPosition());
		navStep = newStep;
		calcStepTime(newStep);
	}

	public Stack<Integer> getPath() {
		return path;
	}
	
	private void calcStepTime(Vector3f target) {
		stepTimeEst = (Vector3f.distance(pos, target) * Window.deltaTime) * (this.speed / 2f);
		stepTimeActual = 0f;
	}
}
