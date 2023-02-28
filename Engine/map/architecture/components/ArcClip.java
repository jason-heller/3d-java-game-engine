package map.architecture.components;

import org.joml.Vector3f;

import geom.AABB;
import geom.Plane;
import scene.entity.Entity;
import scene.entity.util.PhysicsEntity;
import scene.entity.util.PlayerEntity;
import util.Vectors;

public class ArcClip {
	public Vector3f center;
	public Vector3f halfSize;
	public ClipType id;
	public int[] planes;

	public Vector3f getPlaneIntersectionPoint(Plane p1, Plane p2, Plane p3) {
		Vector3f n1 = p1.normal;
		Vector3f n2 = p2.normal;
		Vector3f n3 = p3.normal;

		Vector3f n2Crossn3 = n2.cross(n3);

		float denom = n1.dot(n2Crossn3);

		if (denom == 0f) {
			return null;
		}

		Vector3f r1 = n2Crossn3.mul(p1.dist);
		Vector3f r2 = n3.cross(n1).mul(p2.dist);
		Vector3f r3 = n1.cross(n2).mul(p3.dist);

		return Vectors.div(r1.add(r2).add(r3), denom);
	}

	public boolean interact(Entity entity, boolean isEntering) {
		switch (id) {
		case PLAYER_CLIP:
			if (entity instanceof PlayerEntity)
				return true;
			break;

		case NPC_CLIP:
			if (entity instanceof PhysicsEntity)
				return true;
			break;

		case LADDER:
			if (entity instanceof PhysicsEntity) {
				PhysicsEntity physEnt = (PhysicsEntity) entity;
				physEnt.setClimbing(true);
				return true;
			}
			break;

		case SOLID:
			if (entity instanceof PhysicsEntity)
				return true;
			break;

		case TRIGGER:

			break;

		default:
		}

		return false;
	}

	/*
	 * private boolean isInsideClip(Bsp bsp, ArcClip clip) { int lastEdge =
	 * clip.firstEdge + clip.numEdges; for (int i = clip.firstEdge; i < lastEdge;
	 * i++) { if (bsp.clipEdges[i].texId == -1) continue; if
	 * (bsp.planes[bsp.clipEdges[i].planeId].classify(pos, 1f) == Plane.IN_FRONT) {
	 * return false; } }
	 * 
	 * return true; }
	 */
}
