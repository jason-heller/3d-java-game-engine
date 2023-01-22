package scene.entity.util;


import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.AudioHandler;
import audio.Source;
import core.App;
import dev.Debug;
import geom.AxisAlignedBBox;
import geom.CollideUtils;
import geom.MTV;
import geom.Plane;
import geom.Polygon;
import gl.Window;
import gl.line.LineRender;
import gl.res.Mesh;
import io.Input;
import map.Rail;
import map.architecture.Architecture;
import map.architecture.Material;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcHeightmap;
import map.architecture.components.ArcStaticObject;
import map.architecture.components.ArcTriggerClip;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import util.Colors;
import util.GeomUtil;

public abstract class SkatePhysicsEntity extends Entity {
	
	// Physics flags
	protected boolean grounded = false;
	public boolean previouslyGrounded = false;
	protected boolean sliding = false;
	protected boolean applyGravity = true;
	public boolean solid = true;
	
	// General physics variables
	public static float gravity = 100f;
	public static float maxGravity = -600f;
	public static float friction = 6f;
	public static float airFriction = 0f;
	
	public static float jumpVel = 35f;
	public static float accelSpeed = 1200f, airAccel = 0f;
	public static float maxSpeed = 40f, maxAirSpeed = 1000f;
	protected static float direction = 0f;
	public Vector3f vel = new Vector3f();
	
	// Variables for grinding (move these into separate class?)
	protected Rail grindRail = null;			// Doubles as the flag for grinding
	protected Vector3f grindNormal = null;		// Grind direction
	private Vector3f grindOrigin = null;		// starting point of currently grinded rail
	private float grindSpeed, railLengthSqr;	// Speed of grind, length of rail currently being ridden
	
	protected float grindBalance = 0f;
	private float grindSlip = 0f;
	private float grindBalSpeed = 0f;
	
	protected float grindLen = 0f;
	private Vector3f grindStart = new Vector3f();
	
	// Positional helper vars
	private Vector3f lastPos = new Vector3f();
	private Vector3f boardPos = new Vector3f();
	
	// Refernce to map
	protected Architecture arc;
	
	// Sound variables
	protected Source trickSfxSource = AudioHandler.checkoutSource();
	protected Source rideSfxSource = AudioHandler.checkoutSource();
	protected Material materialStandingOn = Material.ROCK;
	
	// Physics constants
	protected static final float SLIDE_ANGLE = .9f;
	protected static final float EPSILON = 0.01f;
	protected static final float RAIL_GRAVITATION = 3f;
	private static final float STEP_HEIGHT = 2f;
	private static final float RAIL_LINK_ANGLE_THRESHOLD = 0.5f;
	protected static final float MAX_GRIND_BALANCE = 40;
	private static final float INIT_GRIND_BAL_SPEED = 2;
	private static final float INIT_GRIND_SLIP = .01f;
	
	// Cheats
	public static boolean perfectGrind = false;
	
	public SkatePhysicsEntity(String name, Vector3f bounds) {
		super(name);
		bbox = new AxisAlignedBBox(pos, bounds);
		arc = ((PlayableScene)App.scene).getArchitecture();
		
		rideSfxSource.setLooping(true);
	}

	@Override
	public void update(PlayableScene scene) {
		if (Debug.showHitboxes) {
			LineRender.drawBox(bbox.getCenter(), bbox.getBounds(), Colors.YELLOW);
		}
		
		arc = ((PlayableScene)App.scene).getArchitecture();	// TODO: This is temp, make sure this entity spawns AFTER the architecutre
		boardPos.set(Vector3f.sub(pos, new Vector3f(0, bbox.getHeight(), 0)));
	
		if (Debug.viewCollide) {
			arc.drawRails(pos, RAIL_GRAVITATION);
		}
		
		lastPos.set(pos);
		
		if (grindRail != null) {
			Vector3f railInc = Vector3f.mul(grindNormal, grindSpeed * Window.deltaTime);
			pos.add(railInc);
			
			handleGrindState();
			
			float distSqr = Vector3f.distanceSquared(grindOrigin, pos);
			grounded = true;
			
			if (distSqr >= railLengthSqr) {
				Vector3f linkPos = (grindOrigin == grindRail.start) ? grindRail.end : grindRail.start;
				Rail newRail = arc.findLinkingRail(grindRail, linkPos, boardPos, RAIL_GRAVITATION);
				
				if (newRail != null) {
					Vector3f rail1 = Vector3f.sub(grindRail.end, grindRail.start).normalize();
					Vector3f rail2 = Vector3f.sub(newRail.end, newRail.start).normalize();
					
					if (Math.abs(rail1.dot(rail2)) < RAIL_LINK_ANGLE_THRESHOLD) {
						endGrind();
						return;
					}

					setGrindRail(newRail);
					grindLen += Vector3f.distance(grindStart, pos);
					grindStart.set(pos);
				} else {
					endGrind();
				}
			}
			
		} else {
			// Add vertical speed
			if (applyGravity) {
				vel.y = Math.max(vel.y - gravity * Window.deltaTime, maxGravity);
			}

			// Add speed
			pos.x += vel.x * Window.deltaTime;
			pos.y += vel.y * Window.deltaTime;
			pos.z += vel.z * Window.deltaTime;

			applyFriction();
		
			float len = (float)Math.sqrt(vel.x*vel.x + vel.z*vel.z);
			float sfxFactor = Math.min(len / maxSpeed, 1f);
			rideSfxSource.setGain(sfxFactor);
			rideSfxSource.setPitch(sfxFactor);

			previouslyGrounded = grounded;
			grounded = false;
		}
		
		handleCollisions();
		
		if (!grounded && previouslyGrounded) {
			rideSfxSource.stop();
		}
		
		super.update(scene);
	}

	protected void handleGrindState() {
		// Balance
		if (!perfectGrind) {
			grindBalance += grindSlip;
			grindSlip += Window.deltaTime * 0.5f * Math.signum(grindSlip);
		}
		
		//rot.x = 0f;
		rot.z = grindBalance;
		
		if (Input.isDown("left")) {
			grindSlip -= grindBalSpeed * Window.deltaTime;
			grindBalSpeed += Window.deltaTime;
		}
		
		if (Input.isDown("right")) {
			grindSlip += grindBalSpeed * Window.deltaTime;
			grindBalSpeed += Window.deltaTime;
		}
		
		if (Math.abs(grindBalance) > MAX_GRIND_BALANCE) {
			endGrind();
		}
	}

	/**
	 * Called to make the entity begin grinding a rail; Attempts to find a rail to
	 * grind and sets this entity to grinding if one is found. If no rail is found,
	 * and the entity is already grinding, it will terminate the grind state
	 */
	protected void grind() {
		Rail newRail = arc.getNearestRail(boardPos, vel, grindRail, RAIL_GRAVITATION);

		if (newRail != null) {
			rideSfxSource.play("grind");
			
			grindSlip = INIT_GRIND_SLIP * ((Math.random() > 0.5) ? 1f : -1f);
			grindStart.set(pos);
			
			setGrindRail(newRail);
		} else {
			endGrind();
		}
	}

	/** Ends the grinding state, assumes the entity is already grinding
	 */
	protected void endGrind() {
		if (grindRail != null) {
			if (!grounded && previouslyGrounded) {
				rideSfxSource.stop();
			} else {
				rideSfxSource.play("ride");
			}
			
			grindLen += Vector3f.distance(grindStart, pos);
			grindStart.set(pos);
		}
		
		grindRail = null;
		rot.z = 0f;
		grindSlip = 0f;
		grindBalance = 0f;
		grindBalSpeed = INIT_GRIND_BAL_SPEED;
		
		if (grounded) {
			getAnimator().start("idle");
		}
	}

	/**
	 * Begins the grind state using the given rail. This differs from the grind()
	 * function in that this is not meant to begin the grind state, but rather
	 * simply connect the entity to a new rail.
	 * 
	 * @param newRail The rail the entity is grinding
	 */
	private void setGrindRail(Rail newRail) {
		
		grindRail = newRail;
		
		Vector3f edge = Vector3f.sub(grindRail.end, grindRail.start);
		railLengthSqr = edge.lengthSquared();
		Vector3f edgeNormal = edge.normalize();
		Vector3f newPoint = GeomUtil.projectPointOntoLine(boardPos, grindRail.start, edgeNormal);
		
		grindOrigin = grindRail.start;
		grindSpeed = vel.length();
		
		if (vel.dot(edgeNormal) < 0) {
			edgeNormal.negate();
			grindOrigin = grindRail.end;
		}
		
		grindNormal = edgeNormal;
		vel.set(Vector3f.mul(grindNormal, grindSpeed));
		pos.set(newPoint.x, newPoint.y + bbox.getHeight(), newPoint.z);
	}

	public void jump(float height) {
		vel.y = height;
		grounded = false;
		sliding = false;
	}
	
	/** Runs the collision checking and response code for the entity
	 */
	private void handleCollisions() {
		Bsp bsp = arc.bsp;
		
		Vector3f max = Vector3f.add(bbox.getCenter(), bbox.getBounds());
		Vector3f min = Vector3f.sub(bbox.getCenter(), bbox.getBounds());		// Cache these maybe
		
		List<BspLeaf> leaves = bsp.walk(max, min);
		List<ArcFace> faces = bsp.getFaces(leaves);
		
		leaf = bsp.walk(pos);
		
		// Main collision handling
		handleEntityCollisions(leaves);
		handleObjectCollisions(bsp);
		handleHeightmapCollisions(leaves, faces);
		handleFaceCollisions(bsp, faces, 0);

		handleClipCollisions(leaves);
	}

	/**
	 * Handles collisions between this entity and heighmaps
	 * 
	 * @param leaves The list of leaves this entity currently resides in
	 * @param faces  The list of faces from the leaf
	 */
	private void handleHeightmapCollisions(List<BspLeaf> leaves, List<ArcFace> faces) {
		Bsp bsp = arc.bsp;
		for(BspLeaf leaf : leaves) {
			for(short hmapid : leaf.heightmaps) {
				ArcHeightmap hmap = bsp.heightmaps[hmapid];
				ArcFace face = bsp.faces[hmap.getFaceId()];
				if (bsp.planes[face.planeId].normal.y > 0f) {
					if (!hmap.intersects(bbox))
						continue;
					
					float height = hmap.getHeightAt(bbox, bsp.heightmapVerts) + bbox.getHeight();
					if (Float.isFinite(height)) {
						float fudge = (!grounded && vel.y < 0f ? .2f : 0f);
						if (height >= pos.y - fudge) {
							pos.y = height;
							vel.y = 0f;
							grounded = true;
							float blend = hmap.getBlendAt(pos.x, pos.z, bsp.heightmapVerts);

							if (blend >= 0f) {
								int id = (blend < .5f) ? hmap.getTexture1() : hmap.getTexture2();
								materialStandingOn = arc.getTextures()[id].getMaterial();
							}
						}
					}
				} else {
					faces.add(face);
				}
			}
		}
	}

	/**
	 * Handles collisions between this entity and other entities
	 * 
	 * @param leaves The list of leaves this entity currently resides in
	 */
	protected void handleEntityCollisions(List<BspLeaf> leaves) {
		if (!solid) return;
		
		for(BspLeaf leaf : leaves) {
			List<Entity> entities = EntityHandler.getEntities(leaf);
			
			if (entities == null)
				continue;
			
			for(Entity entity : entities) {
				if (entity == this) continue;
				if (entity instanceof SkatePhysicsEntity) {
					SkatePhysicsEntity physEnt = (SkatePhysicsEntity) entity;
					
					if (!physEnt.solid)
						continue;

					handleAabbCollision(physEnt.bbox);
				}
			}
		}
	}
	
	/**
	 * Handles collisions between this entity and axis-aligned bounding boxes
	 * 
	 * @param otherBox the AABB to collide with
	 */
	private void handleAabbCollision(AxisAlignedBBox otherBox) {
		MTV mtv = bbox.collide(otherBox);

		if (mtv != null) {
			if (mtv.getAxis().y > .5f) {
				vel.y = 0f;
				grounded = true;
				pos.y = otherBox.getCenter().y + bbox.getBounds().y + otherBox.getBounds().y;
			} else {
				vel.add(mtv.getMTV());
			}
		}
	}

	/**
	 * Handles collisions between this entity and any static object in the map
	 * 
	 * @param bsp The BSP of the map
	 */
	protected void handleObjectCollisions(Bsp bsp) {
		if (!solid) return;
		
		List<ArcStaticObject> objects = bsp.objects.getObjects(leaf);
		
		if (objects == null) 
			return;
		
		for(ArcStaticObject obj : objects) {
			if (obj.solidity == 0)
				continue;
			
			AxisAlignedBBox otherBox = obj.getBBox();
			if (bbox.intersects(otherBox)) {
				if (obj.solidity == 1) {
					handleAabbCollision(otherBox);
				} else {
					Mesh model = bsp.objects.getModel(obj.model);
					if (model.getNavMesh() == null)
						continue;
					
					Vector3f[] navMesh = model.getNavMesh();
					for(int i = 0; i < navMesh.length; i += 3) {
						Vector3f p1 = Vector3f.add(navMesh[i], obj.pos);
						Vector3f p2 = Vector3f.add(navMesh[i + 1], obj.pos);
						Vector3f p3 = Vector3f.add(navMesh[i + 2], obj.pos);
						Polygon tri = new Polygon(p1, p2, p3);

						MTV mtv = bbox.collide(tri);
						if (mtv != null) {
							resolveTriCollision(mtv, tri);
						}
					}
				}
			}
		}
	}
	
	/** Handles collisions between this entity and the map geometry
	 * @param bsp The map's BSP data
	 * @param faces The list of leaves this entity currently resides in
	 * @param iteractions The current number of iterations through the face list performed for collision checking, used for recursion
	 */
	private void handleFaceCollisions(Bsp bsp, List<ArcFace> faces, int iterations) {
		MTV nearest = null;
		ArcFace nearestFace = null;
		float smallestDepth = 2f;

		for(ArcFace face : faces) {
			
			if (face.texMapping == -1)		// Don't collide against invisible geometry
				continue;
			
			Plane plane = bsp.planes[face.planeId];
			Vector3f normal = plane.normal;
			
			MTV mtv = CollideUtils.bspFaceBoxCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, normal, bbox);
			
			if (mtv != null && smallestDepth >= mtv.getDepth()) {
				nearest = mtv;
				nearestFace = face;
				smallestDepth = mtv.getDepth();
			}
		}
		
		if (nearest != null) {
			resolveFaceCollision(bsp, nearest, nearestFace);
			faces.remove(nearestFace);
			
		} else {
			return;
		}
		
		if (iterations != 7) {
			handleFaceCollisions(bsp, faces, iterations + 1);
		}
	}
	
	/**
	 * Handles collisions between this entity and clips
	 * 
	 * @param leaves The list of leaves this entity currently resides in
	 */
	private void handleClipCollisions(List<BspLeaf> leaves) {
		Bsp bsp = arc.bsp;
		for(BspLeaf leaf : bsp.leaves) {
			for(short clipId : leaf.clips) {
				ArcClip clip = bsp.clips[clipId];
				
				if (arc.getActiveTrigger(this) == clip)
					continue;
				
				Plane[] planes = new Plane[clip.numPlanes];
				for(int i = 0; i < clip.numPlanes; i++) {
					planes[i] = bsp.planes[bsp.clipPlaneIndices[clip.firstPlane + i]];
				}
				
				MTV mtv = CollideUtils.convexHullBoxCollide(planes, bbox);
				if (mtv != null) {
					boolean doCollide = clip.interact(this, true);
					if (clip instanceof ArcTriggerClip) {
						arc.setTriggerActive(this, (ArcTriggerClip) clip);
					}
					
					if (doCollide) {
						resolveFaceCollision(bsp, mtv, null);
					}
				}
			}
		}
	}
	
	private void resolveTriCollision(MTV mtv, Polygon tri) {
		if (mtv.getAxis().y < .5f) {
			bbox.getCenter().y += STEP_HEIGHT;
			MTV stepMtv = bbox.collide(tri);
			bbox.getCenter().y -= STEP_HEIGHT;
			if (stepMtv == null) {
				if (Math.abs(tri.normal.y) > .5f) {
					collideWithFloor(tri.getPlane());
				}
				return;
			}
		}
		
		if (Debug.viewCollide && mtv != null) {
			LineRender.drawTriangle(tri, Colors.RED);
		}
		
		if (mtv.getAxis().y >= .5f) {
			collideWithFloor(tri.getPlane());
			return;
		}
		
		pos.add(mtv.getMTV());
		vel.add(Vector3f.div(mtv.getMTV(), Window.deltaTime));
	}
	
	private void resolveFaceCollision(Bsp bsp, MTV mtv, ArcFace face) {
		
		if (face == null) {
			pos.add(mtv.getMTV());
			return;
		}
		
		if (Debug.viewCollide && mtv != null) {
			for(int i = face.firstEdge; i < face.numEdges + face.firstEdge; i++) {
				ArcEdge edge = bsp.edges[Math.abs(bsp.surfEdges[i])];
				LineRender.drawLine(bsp.vertices[edge.start], bsp.vertices[edge.end], Colors.RED);
			}
		}
		
		Plane plane = mtv.getPlane();
		if (plane == null) {
			plane = bsp.planes[mtv.getFace().planeId];		// HACKY
		}

		// If floor..
		if (plane.normal.y >= .49f) {
			
			if (mtv.getAxis().y < .5f) {
				return;
			}
			
			
			grounded = true;
			
			if (face != null) {
				int id = bsp.getTextureMappings()[face.texMapping].textureId;
				materialStandingOn = arc.getTextures()[id].getMaterial();
			}

			pos.add(mtv.getMTV());
			//float ry = rot.y;
			//GeomUtil.rotateAboutNormal(rot, plane.normal, Vector3f.Y_AXIS);
			//rot.y = ry;
		} else {
			// This fudges the player if theyre close to the top of the wall
			pos.y += 2f;
			MTV wallHopMtv = CollideUtils.bspFaceBoxCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, plane.normal, bbox);
			pos.y -= 2f;
			
			if (wallHopMtv == null) {
				return;
			}
			
			if (grindRail != null)
				endGrind();
			
			
			// Handle velocity depending on direction to wall
			float facing = vel.dot(new Vector3f(plane.normal.z, 0f, -plane.normal.x)) > 0 ? 180 : 0;
			pos.add(mtv.getMTV());
			
			direction = (float)Math.toDegrees(Math.atan2(plane.normal.z, plane.normal.x)) + facing;
			float len = new Vector3f(vel.x, 0f, vel.z).length();
			float vy = vel.y;
			vel.set(Vector3f.sub(pos, lastPos));
			vel.setLength(len);
			vel.y = vy;
		}
	}

	protected void collideWithFloor(Plane plane) {
		grounded = true;
	}
	
	/**
	 * Applies a force to this entity
	 * 
	 * @param direction the direction of the force
	 * @param magnitude the magnitude of the force
	 */
	public void accelerate(Vector3f direction, float magnitude) {
		final float projVel = Vector3f.dot(vel, direction); // Vector projection of Current vel onto accelDir.
		float accelVel = magnitude * Window.deltaTime; // Accelerated vel in direction of movment

		// If necessary, truncate the accelerated vel so the vector projection does
		// not exceed max_vel
		final float speedCap = grounded ? maxSpeed : maxAirSpeed;

		if (projVel + accelVel > speedCap) {
			accelVel = speedCap - projVel;
		}

		vel.x += direction.x * accelVel;
		vel.y += direction.y * accelVel;
		vel.z += direction.z * accelVel;
	}

	/**
	 * Applies friction the entity when called, should be called once per tick
	 */
	private void applyFriction() {
		if (vel.lengthSquared() < .01f)
			return;
		
		if (!sliding && previouslyGrounded) {
			final float speed = vel.length();
			if (speed != 0) {
				float drop = speed * friction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				vel.mul(offset); // Scale the vel based on friction.
			}
		} else if (airFriction != 0f && !sliding) {
			final float speed = new Vector2f(vel.x, vel.z).length();
			if (speed != 0f) {
				final float drop = speed * airFriction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				vel.set(vel.x * offset, vel.y, vel.z * offset); // Scale the vel based on
																					// friction.
			}
		}
	}
	
	public Material getMaterialStandingOn() {
		return materialStandingOn;
	}

	public boolean isGrounded() {
		return grounded;
	}

	public boolean isSliding() {
		return sliding;
	}
	
	public AxisAlignedBBox getBBox() {
		return bbox;
	}
	
	public float getGrindLen() {
		return this.grindLen;
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		trickSfxSource.delete();
		rideSfxSource.delete();
	}
}
