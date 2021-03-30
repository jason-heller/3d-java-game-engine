package map.architecture.vis;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import dev.Debug;
import geom.Plane;
import gl.line.LineRender;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcObjects;

public class Bsp {
	private static final float EPSILON = 0.0001f;
	
	public Plane[] planes;
	public BspNode[] nodes;
	public BspLeaf[] leaves;
	public ArcFace[] faces;

	public Vector3f[] vertices;
	public ArcEdge[] edges;
	public int[] surfEdges;
	public short[] leafFaceIndices;

	public ArcClip[] clips;
	public int[] clipEdges;

	public ArcObjects objects;
	
	public void cleanUp() {
		for(BspLeaf leaf : leaves) {
			leaf.cleanUp();
		}
		objects.cleanUp();
	}

	// The children[] members are the two children of this node; if positive, they are node indices; if negative, 
	// the value (-1-child) is the index into the leaf array (e.g., the value -100 would reference leaf 99). 
	public BspLeaf walk(Vector3f position) {
		int nextNode = 0;
		BspNode node;
		
		while(nextNode > -1) {
			node = nodes[nextNode];
			
			if (planes[node.planeNum].classify(position, EPSILON) == Plane.IN_FRONT) {
				nextNode = node.childrenId[0];
			} else {
				nextNode = node.childrenId[1];
			}
		}
		
		return leaves[-1-nextNode];
	}
	
	public List<ArcFace> getFaces(List<BspLeaf> leaves) {
		List<ArcFace> allFaces = new ArrayList<>();
		for(BspLeaf leaf : leaves) {
			ArcFace[] arcFaces = getFaces(leaf);
			for(ArcFace f : arcFaces) {
				allFaces.add(f);
			}
		}
		
		return allFaces;
	}
	
	public List<BspLeaf> walk(Vector3f max, Vector3f min) {
		BspNode node = nodes[0];
		return walk(node, max, min);
	}
	
	public List<BspLeaf> walk(BspNode node, Vector3f boxMax, Vector3f boxMin) {
		List<BspLeaf> allLeaves = new ArrayList<>();
		
		Vector3f padding = new Vector3f(1f,1f,1f);		// Map editor generates bounds a little weirdly
		Vector3f max = Vector3f.add(boxMax, padding);
		Vector3f min = Vector3f.sub(boxMin, padding);
		
		int child0 = node.childrenId[0];
		int child1 = node.childrenId[1];
		
		if (child0 < -1) {
			if (leaves[-1-child0].intersects(max, min)) {
				allLeaves.add(leaves[-1-child0]);
				
				if (Debug.showLeafs) {
					Vector3f center = Vector3f.add(leaves[-1-child0].min, leaves[-1-child0].max).div(2f);
					Vector3f bounds = Vector3f.sub(leaves[-1-child0].max, leaves[-1-child0].min).div(2f);
					LineRender.drawBox(center, bounds, Vector3f.X_AXIS);
				}
			}
		} else if (nodes[child0].intersects(max, min)) {
			allLeaves.addAll(walk(nodes[child0], max, min));
		}
		
		if (child1 < -1) {
			if (leaves[-1-child1].intersects(max, min)) {
				allLeaves.add(leaves[-1-child1]);
				
				if (Debug.showLeafs) {
					Vector3f center = Vector3f.add(leaves[-1-child1].min, leaves[-1-child1].max).div(2f);
					Vector3f bounds = Vector3f.sub(leaves[-1-child1].max, leaves[-1-child1].min).div(2f);
					LineRender.drawBox(center, bounds, Vector3f.X_AXIS);
				}
			}
		} else if (nodes[child1].intersects(max, min)) {
			allLeaves.addAll(walk(nodes[child1], max, min));
		}
		
		return allLeaves;
	}

	public Plane[] getPlanes(BspLeaf leaf) {
		if (leaf.clusterId == -1) {
			return new Plane[] {};
		}
		
		Plane[] leafPlanes = new Plane[leaf.numFaces];
		
		for(int i = 0; i < leaf.numFaces; i++) {
			leafPlanes[i] = planes[faces[leafFaceIndices[leaf.firstFace+i]].planeId];
		}
		
		return leafPlanes;
	}

	public ArcFace[] getFaces(BspLeaf leaf) {
		if (leaf.clusterId == -1) {
			return new ArcFace[] {};
		}
		
		ArcFace[] leafPlanes = new ArcFace[leaf.numFaces];
		
		for(int i = 0; i < leaf.numFaces; i++) {
			leafPlanes[i] = faces[leafFaceIndices[leaf.firstFace+i]];
		}
		
		return leafPlanes;
	}

	/*public boolean obbHullIntersection(BoundingBox obb, int firstFace, int numFaces) {
		int lastFace = firstFace + numFaces;
		for (int i = firstFace; i < lastFace; i++) {
			if (planes[faces[i].planeId].classify(obb.center, .001f) == Plane.IN_FRONT) {
				return false;
			}
		}

		return true;
	}*/
	
}
