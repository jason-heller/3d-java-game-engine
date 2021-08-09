package scene.entity.util;

import java.util.Stack;

import org.joml.Vector3f;

import core.Application;
import dev.Debug;
import gl.line.LineRender;
import map.architecture.ArcUtil;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcNavNode;
import map.architecture.components.ArcNavigation;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import util.Colors;

public abstract class NavigableEntity extends PhysicsEntity {

	public int navPathNode = -1;
	protected ArcNavigation navigation = null;

	private Stack<Integer> path;
	public int currentNodeId;
	
	protected float thinkInterval = .5f;
	
	protected float speed = 25f;
	protected Vector3f navTarget = new Vector3f();
	
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
				
				Bsp bsp = ((PlayableScene)Application.scene).getArchitecture().bsp;
				
				int navEdgeId = currentNode.getEdges()[neighborId];
				//ArcFace face = bsp.faces[currentNode.getFaceId()];
				ArcEdge edge = bsp.edges[navEdgeId];

				Vector3f p1 = bsp.vertices[edge.start], p2 = bsp.vertices[edge.end];
				Vector3f edgeVec = Vector3f.sub(p2, p1);
				navStep = Vector3f.add(p1, p2).div(2f);
				
				// Code for traversing a node
				
				// If heading directly to the edge is viable, it should be done
				float bboxWidSqr = 66;
				// Get length of edge we must cross
				float edgeLenSqr = edgeVec.dot(edgeVec);
				// Get hueristic of edge's center to position
				float hueristicA = Vector3f.distanceSquared(pos, navStep);
				// Get projection length from p1 to position onto the edge vector
				float ptProjLen = Vector3f.sub(pos, p1).dot(edgeVec);
				Vector3f directPos = Vector3f.add(p1, Vector3f.mul(edgeVec, ptProjLen / edgeLenSqr));
				float hueristicB = Vector3f.distanceSquared(pos, directPos);
				// If this projection falls on the edge
				if (hueristicA > hueristicB && ptProjLen >= bboxWidSqr && ptProjLen < edgeLenSqr - bboxWidSqr) {

					navStep = directPos;
				}
				
				// If the difference between navStep and the edge are too similar, head towards node center
				/*Vector3f movementVec = Vector3f.sub(navStep, pos);
				// u = edgevec, v = movementVec
				float crossX = edgeVec.y * movementVec.z + edgeVec.z * movementVec.y;
				float crossY = edgeVec.x * movementVec.z + edgeVec.z * movementVec.x;
				float crossZ = edgeVec.x * movementVec.y + edgeVec.y * movementVec.x;
				float crossN = crossX * crossX + crossY * crossY + crossZ * crossZ;
				Console.log(crossN);
				if (crossN < .1f) {
					navStep = currentNode.getPosition();
				}*/
			}
		}
	
		if (navStep != null) {
			Vector3f dir = Vector3f.sub(navStep, pos);	//  TODO: This needs to stay on the current & next face
			float len = dir.length();
			if (len > .001f) {
				dir.set(dir.x, 0f, dir.z).div(len);
				this.accelerate(dir, speed);
			}
		}
		
		if (Debug.viewNavPath && navStep != null) {
			Bsp bsp = ((PlayableScene)Application.scene).getArchitecture().bsp;
			Vector3f lastPt = navTarget;
			
			ArcNavNode node = navigation.getNode(currentNodeId);
			for(int id : node.getFaceIds()) {
				ArcFace face = bsp.faces[id];
				ArcUtil.drawFaceHighlight(bsp, face, Colors.alertColor());
			}
			
			for(int i = 1; i < path.size(); i++) {
				int nextNodeId = path.get(i-1);
				ArcNavNode node1 = navigation.getNode(path.get(i));
				// ArcNavNode node2 = navigation.getNode(nextNodeId);
				
				short[] neighbors = node1.getNeighbors();
				
				int neighborId = -1;
				for(int j = 0; j < neighbors.length; j++) {
					if (neighbors[j] == nextNodeId) {
						neighborId = j;
						break;
					}
				}
				
				int navEdgeId = node1.getEdges()[neighborId];
				ArcFace face = bsp.faces[node1.getFaceIds()[0]];
				ArcEdge edge = bsp.edges[navEdgeId];
				Vector3f p1 = bsp.vertices[edge.start], p2 = bsp.vertices[edge.end];
				Vector3f pt = Vector3f.add(p1, p2).div(2f);
				
				LineRender.drawLine(Vector3f.add(pt, new Vector3f(0,3,0)), Vector3f.add(lastPt, new Vector3f(0,3,0)), Colors.RED);
				lastPt = pt;
			}
			// 
			Vector3f step = Vector3f.add(navStep, new Vector3f(0,3,0));
			LineRender.drawLine(Vector3f.add(lastPt, new Vector3f(0,3,0)),step, Colors.RED);
			//LineRender.drawLine(step,Vector3f.add(pos, new Vector3f(0,3-getBBox().getHeight(),0)), Colors.RED);
		}
	}

	public Stack<Integer> getPath() {
		return path;
	}

	public void setNavigation(ArcNavigation navigation) {
		this.navigation = navigation;
	}
}
