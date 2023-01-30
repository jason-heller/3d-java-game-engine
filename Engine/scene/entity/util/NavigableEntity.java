package scene.entity.util;

import java.util.Stack;

import org.joml.Vector3f;

import dev.Debug;
import gl.line.LineRender;
import map.architecture.components.ArcNavNode;
import map.architecture.components.ArcNavigation;
import scene.PlayableScene;
import util.Colors;
import util.Vectors;

public abstract class NavigableEntity extends PhysicsEntity {

	public int navPathNode = -1;
	protected ArcNavigation navigation = null;

	private Stack<Integer> path;
	public int currentNodeId;
	
	protected float thinkInterval = .5f;
	
	protected float speed = 25f;
	protected Vector3f navTarget = new Vector3f(), dir = new Vector3f();
	
	public NavigableEntity(String name, Vector3f bounds) {
		super(name, bounds);

		path = new Stack<>();
		
	}
	
	public void setTarget(Vector3f navTarget) {
		this.navTarget = navTarget;
		if (navTarget == null) {
			return;
		}

	}

	public Vector3f getTarget() {
		return navTarget;
	}
	
	@Override
	public void update(PlayableScene scene) {
		navigation.navigateTo(this, navTarget);
		
		
		super.update(scene);
		Vector3f navStep = null;
		
		if (navTarget != null) {
			if (path.size() == 0) {
				navStep = navTarget;
			} else {
				ArcNavNode currentNode = navigation.getNode(currentNodeId);
				// Find which neighbor we are going to
				short[] neighbors = currentNode.getNeighbors();
				int nextNodeId = path.get(path.size() - 1);
				int neighborId = -1;							// Could be cached
				for(int i = 0; i < neighbors.length; i++) {
					if (neighbors[i] == nextNodeId) {
						neighborId = i;
						break;
					}
				}
				
				if (neighborId == -1) return;

				// Bsp bsp = ((PlayableScene)App.scene).getArchitecture().bsp;

				// int nextNodeId = currentNode.getNeighbors()[neighborId];
				// ArcFace face = bsp.faces[currentNode.getFaceId()];
				// ArcEdge edge = bsp.edges[nextNodeId];
				
				navStep = navigation.getNode(nextNodeId).getPosition();
			}
		}
	
		if (navStep != null) {
			Vector3f newDir = Vectors.sub(navStep, position);
			newDir.y = 0;
			float len = newDir.length();
			if (len >= 1f) {
				dir.set(newDir.x, 0f, newDir.z).div(len);
				this.accelerate(dir, speed);
			}
		}
		
		if (Debug.viewNavPath && navStep != null) {
			// Bsp bsp = ((PlayableScene)App.scene).getArchitecture().bsp;
			Vector3f lastPt = navTarget;
			
			// ArcNavNode node = navigation.getNode(currentNodeId);

			// ArcFace face = bsp.faces[node.getFaceId()];
			// ArcUtil.drawFaceHighlight(bsp, face, Colors.alertColor());
			
			for(int i = 1; i < path.size(); i++) {
				// int nextNodeId = path.get(i-1);
				ArcNavNode node1 = navigation.getNode(path.get(i));
				// ArcNavNode node2 = navigation.getNode(nextNodeId);
				
				// short[] neighbors = node1.getNeighbors();
				
				/* int neighborId = -1;
				for(int j = 0; j < neighbors.length; j++) {
					if (neighbors[j] == nextNodeId) {
						neighborId = j;
						break;
					}
				}*/
				
				
				LineRender.drawLine(Vectors.add(node1.getPosition(), new Vector3f(0,3,0)), Vectors.add(lastPt, new Vector3f(0,3,0)), Colors.RED);
				lastPt = node1.getPosition();
			}
			// 
			Vector3f step = Vectors.add(navStep, new Vector3f(0,3,0));
			LineRender.drawLine(Vectors.add(lastPt, new Vector3f(0,3,0)),step, Colors.RED);
			//LineRender.drawLine(step,Vectors.add(pos, new Vector3f(0,3-getBBox().getHeight(),0)), Colors.RED);
		}
	}

	public Stack<Integer> getPath() {
		return path;
	}

	public void setNavigation(ArcNavigation navigation) {
		this.navigation = navigation;
	}
}
