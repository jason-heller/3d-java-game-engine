package geom;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;
import scene.entity.Entity;
import scene.entity.util.SkatePhysicsEntity;
import util.MathUtil;
import util.Vectors;

public class EntityCollisionResolver {

	private static final float FLOOR_ALLOWABLE_DIFFERENCE = 0.5f;
	private static final float WALL_FORGIVENESS = 2;		// If the player is BARELY over the wall by this many units, just pop them up
	
	private SkatePhysicsEntity entity;
	private BoundingBox box;
	
	public EntityCollisionResolver(SkatePhysicsEntity entity) {
		this.entity = entity;
		box = entity.getBBox();
	}

	public void resolve(Bsp bsp, ArcFace face) {
		final Plane plane = bsp.planes[face.planeId];
		final Vector3f planeNormal = plane.normal;
		final Vector3f pos = entity.position;
		final Vector3f vel = entity.localVelocity;
		
		// Are we aligned with the face normal enough?
		if (box.Y.dot(planeNormal) < FLOOR_ALLOWABLE_DIFFERENCE) {
			rotateTo(planeNormal, SkatePhysicsEntity.direction);
			
			Vector3f escapeVector = new Vector3f(box.Y);
			float depthToCenter = plane.signedDistanceTo(box.center);
			escapeVector.mul(box.getHeight() - depthToCenter);
			
			pos.add(escapeVector);
			
			// lastFloor = face;
			entity.setGrounded(true);
			
		} else if (box.Y.y > .5f) {
			
			// Ignore this collision if we're near its very top
			box.center.y += WALL_FORGIVENESS;
			MTV wallHopMtv = CollideUtils.faceCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, planeNormal, box);
			box.center.y -= WALL_FORGIVENESS;

			if (wallHopMtv == null)
				return;
			
			// Handle velocity depending on direction to wall
			float facing = vel.dot(new Vector3f(plane.normal.z, 0f, -plane.normal.x)) > 0 ? MathUtil.PI : 0f;
			pos.add(plane.normal);

			float newDirection = (float) Math.atan2(plane.normal.z, plane.normal.x) + facing;
			float vy = vel.y;
			
			if (!entity.isGrounded() || Math.abs(SkatePhysicsEntity.direction - newDirection) < MathUtil.HALFPI/2f) {
				float vLen = new Vector3f(vel.x, 0f, vel.z).length();
				vel.set(plane.normal.z, 0f, -plane.normal.x);
				
				if (facing == 0f)
					vel.negate();
				
				vel.mul(vLen);
			} else {
				vel.zero();
			}

			vel.y = vy;
			SkatePhysicsEntity.direction = newDirection;
			pos.add(Vectors.mul(plane.normal, box.getWidth() * 0.16f));
		} else {
			// Bail
		}
	}
	
	private void rotateTo(Vector3f up, float xzRad) {
		Quaternionf toFace = new Quaternionf();
		toFace.rotationTo(0, 1, 0, up.x, up.y, up.z);
		toFace.rotateY(-xzRad);
		
		entity.rotation.set(toFace);
		entity.localVelocity.rotate(toFace);
		
		box.setRotation(toFace);

		/*if (plane.normal.y < .99f) {
			Quaternionf diff = new Quaternionf().rotationTo(bbox.Y, plane.normal);
			velocity.rotate(diff);
		}*/
	}
}
