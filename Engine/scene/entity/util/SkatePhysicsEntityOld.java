package scene.entity.util;


import java.util.List;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.AudioHandler;
import audio.Source;
import core.App;
import dev.Debug;
import dev.cmd.Console;
import geom.BoundingBox;
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
import util.MathUtil;
import util.Vectors;

public abstract class SkatePhysicsEntityOld extends Entity {
	
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
	public static float accelSpeed = 400f, airAccel = 0f;
	public static float maxSpeed = 40f, maxAirSpeed = 1000f;
	public static float direction = 0f;
	private float speedRamp = 100f;
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
	
	private float speed = 0f;
	
	// Positional helper vars
	private Vector3f lastPos = new Vector3f();
	protected Vector3f boardPos = new Vector3f();
	private Vector3f bboxPos = new Vector3f();
	
	// Refernce to map
	protected Architecture arc;
	
	// Sound variables
	protected Source trickSfxSource = AudioHandler.checkoutSource();
	protected Source rideSfxSource = AudioHandler.checkoutSource();
	protected Material contactMaterial = Material.ROCK;
	private boolean rideSfxPlaying = false;
	
	private boolean collidedWithMaterial = false;
	private ArcFace lastFloor;

	// Vert variables
	private float vertExitSpeed = Float.NaN;
	private float vertExitDir;
	protected Vector3f vertAxis;
	
	// Physics constants
	protected static final float SLIDE_ANGLE = .9f;
	protected static final float EPSILON = 0.01f;
	protected static final float RAIL_GRAVITATION = 3f;
	private static final float VERT_TRANSITION_THRESHOLD = .6f;
	private static final float RAIL_LINK_ANGLE_THRESHOLD = 0.5f;
	protected static final float MAX_GRIND_BALANCE = 40;
	private static final float INIT_GRIND_BAL_SPEED = 2;
	private static final float INIT_GRIND_SLIP = .01f;
	private static final float WALL_FORGIVENESS = 2;		// If the player is BARELY over the wall by this many units, just pop them up
	
	// Cheats
	public static boolean perfectGrind = false;
	
	public SkatePhysicsEntityOld(String name, Vector3f bounds) {
		super(name);
		bbox = new BoundingBox(bboxPos, bounds);
		arc = ((PlayableScene)App.scene).getArchitecture();
		
		rideSfxSource.setLooping(true);
	}
	
	@Override
	public void update(PlayableScene scene) {

		speed = new Vector3f(vel.x, vel.z, 0f).length();
		
		if (Debug.showHitboxes) {
			LineRender.drawBox(bbox, Colors.YELLOW);
		}
		
		arc = ((PlayableScene)App.scene).getArchitecture();	// TODO: This is temp, make sure this entity spawns AFTER the architecutre
		
		updatePositions();
	
		if (Debug.showCollisions) {
			arc.drawRails(position, this.grindRail, bbox.getWidth());
		}
		
		lastPos.set(position);
		
		if (grindRail != null) {
			Vector3f railInc = Vectors.mul(grindNormal, grindSpeed * Window.deltaTime);
			position.add(railInc);
			
			
			handleGrindState();
			
			float distSqr = grindOrigin.distanceSquared(boardPos);

			grounded = true;
			if (grindRail != null && distSqr >= railLengthSqr) {
				
				Vector3f linkPos = new Vector3f((grindOrigin == grindRail.getStart()) ? grindRail.getEnd() : grindRail.getStart());

				Rail newRail = arc.findLinkingRail(grindRail, linkPos, bbox.getWidth());
				
				if (newRail != null) {

					Vector3f rail1 = Vectors.sub(grindRail.getEnd(), grindRail.getStart()).normalize();
					Vector3f rail2 = Vectors.sub(newRail.getEnd(), newRail.getStart()).normalize();
					
					if (Math.abs(rail1.dot(rail2)) < RAIL_LINK_ANGLE_THRESHOLD) {
						endGrind();
						return;
					}

					setGrindRail(newRail);
					
					grindLen += grindStart.distance(position);
					grindStart.set(position);
					
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
			position.x += vel.x * Window.deltaTime;
			position.y += vel.y * Window.deltaTime;
			position.z += vel.z * Window.deltaTime;

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
		
		if (vertAxis != null) {
			direction = vertExitDir + ((-vel.y + vertExitSpeed) / (vertExitSpeed * 2f)) * MathUtil.PI;
			rotation.identity();
			
			rotation.rotateAxis(-MathUtil.HALFPI, vertAxis);
			rotation.rotateY(-direction);
		}
		
		super.update(scene);
		
		if (!grounded) 
			contactMaterial = null;
	}

	private void updatePositions() {
		boardPos.set(Vectors.sub(position, new Vector3f(0, bbox.getHeight(), 0)));
		bboxPos.set(boardPos);
		bboxPos.add(Vectors.mul(bbox.Y, bbox.getHeight()));
	}

	protected void handleGrindState() {
		// Balance
		if (!perfectGrind) {
			grindBalance += grindSlip;
			grindSlip += Window.deltaTime * 0.5f * Math.signum(grindSlip);
		}
		
		rotation.rotateZ((float)Math.toRadians(grindBalance));
		
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
		Rail newRail = arc.getNearestRail(boardPos, vel, grindRail, bbox.getWidth(), RAIL_GRAVITATION);

		if (newRail != null) {
			rideSfxSource.play("grind");
			rideSfxPlaying = true;
			contactMaterial = null;
			
			grindSlip = INIT_GRIND_SLIP * ((Math.random() > 0.5) ? 1f : -1f);
			grindStart.set(position);
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
				rideSfxPlaying = false;
			}
			
			grindRail = null;
			determineRideSound();
			
			grindLen += grindStart.distance(position);
			grindStart.set(position);
		}
		
		rotation.z = 0f;
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
		
		Vector3f edge = Vectors.sub(grindRail.getEnd(), grindRail.getStart());
		railLengthSqr = edge.lengthSquared();
		edge.normalize();
		
		Vector3f newPoint = GeomUtil.projectPointOntoLine(boardPos, grindRail.getStart(), edge);
		
		grindOrigin = grindRail.getStart();
		grindSpeed = speed;
		
		if (vel.dot(edge) < 0) {
			edge.negate();
			grindOrigin = grindRail.getEnd();
		}
		
		grindNormal = edge;
		vel.set(Vectors.mul(grindNormal, grindSpeed));
		position.set(newPoint.x, newPoint.y + bbox.getHeight(), newPoint.z);

	}

	public void jump(float height) {
		vel.y = Math.max(vel.y / 2f + height, height);
		position.y += 1f;
		grounded = false;
		sliding = false;
	}
	
	/** Runs the collision checking and response code for the entity
	 */
	private void handleCollisions() {
		Bsp bsp = arc.bsp;
		
		Vector3f max = Vectors.add(bbox.getCenter(), bbox.getHalfSize());
		Vector3f min = Vectors.sub(bbox.getCenter(), bbox.getHalfSize());		// Cache these maybe
		
		List<BspLeaf> leaves = bsp.walk(max, min);
		List<ArcFace> faces = bsp.getFaces(leaves);
		
		leaf = bsp.walk(position);
		
		collidedWithMaterial = false;
		lastFloor = null;
		
		// Main collision handling
		handleEntityCollisions(leaves);
		handleObjectCollisions(bsp);
		handleHeightmapCollisions(leaves, faces);
		
		// Hacky, but glues the player to slanted floors despite float imprecisions
		if (previouslyGrounded && grindRail == null && bbox.Z.y < .5f) {
			bbox.center.y --;
			for(ArcFace face : faces) {
				MTV mtv = CollideUtils.faceCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, bsp.planes[face.planeId].normal, bbox);
				
				if (mtv != null) {
					if (!collidedWithMaterial && !Float.isNaN(CollideUtils.raycastMapGeometry(bsp, face,
							new Vector3f(position.x + .2f, position.y, position.z + .2f), Vectors.NEGATIVE_Y))) {
						checkContactMaterial(face);
						collidedWithMaterial = true;
					}
					bbox.center.y ++;
					resolveFaceCollision(bsp, mtv, face);
					break;
				}
			}
			
		}
		
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
						if (height >= position.y - fudge) {
							position.y = height;
							vel.y = 0f;
							grounded = true;
							float blend = hmap.getBlendAt(position.x, position.z, bsp.heightmapVerts);

							if (blend >= 0f) {
								int id = (blend < .5f) ? hmap.getTexture1() : hmap.getTexture2();
								checkContactMaterial(arc.getTextures()[id].getMaterial());
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
				if (entity instanceof SkatePhysicsEntityOld) {
					SkatePhysicsEntityOld physEnt = (SkatePhysicsEntityOld) entity;
					
					if (!physEnt.solid)
						continue;

					handleBBoxCollision(physEnt.bbox);
				}
			}
		}
	}
	
	/**
	 * Handles collisions between this entity and axis-aligned bounding boxes
	 * 
	 * @param otherBox the AABB to collide with
	 */
	private void handleBBoxCollision(BoundingBox other) {
		if (bbox.intersects(other)) {
			if (bbox.getIntersectionAxis().y > .5f) {
				vel.y = 0f;
				grounded = true;
				position.y = other.getCenter().y + bbox.getHalfSize().y + other.getHalfSize().y;
			} else {
				Vector3f mtv = new Vector3f(bbox.getIntersectionAxis()).mul(bbox.getIntersectionDepth());
				vel.add(mtv);
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
			
			BoundingBox otherBox = obj.getBBox();
			if (bbox.intersects(otherBox)) {
				if (obj.solidity == 1) {
					handleBBoxCollision(otherBox);
				} else {
					Mesh model = bsp.objects.getModel(obj.model);
					if (model.getNavMesh() == null)
						continue;
					
					Vector3f[] navMesh = model.getNavMesh();
					for(int i = 0; i < navMesh.length; i += 3) {
						Vector3f p1 = Vectors.add(navMesh[i], obj.pos);
						Vector3f p2 = Vectors.add(navMesh[i + 1], obj.pos);
						Vector3f p3 = Vectors.add(navMesh[i + 2], obj.pos);
						Polygon tri = new Polygon(p1, p2, p3);

						/*MTV mtv = bbox.intersects(tri);
						if (mtv != null) {
							resolveTriCollision(mtv, tri);
						}*/
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
		updatePositions();
		
		for(ArcFace face : faces) {
			
			if (face.texMapping == -1)		// Don't collide against invisible geometry
				continue;
			
			Plane plane = bsp.planes[face.planeId];
			Vector3f normal = plane.normal;
			
			MTV mtv = CollideUtils.faceCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, normal, bbox);
			
			if (mtv != null) {

				if (!collidedWithMaterial && !Float.isNaN(CollideUtils.raycastMapGeometry(bsp, face,
						new Vector3f(position.x + .2f, position.y, position.z + .2f), Vectors.NEGATIVE_Y))) {
					checkContactMaterial(face);
					collidedWithMaterial = true;
				}
				
				if (smallestDepth >= mtv.getDepth()) {
					nearest = mtv;
					nearestFace = face;
					smallestDepth = mtv.getDepth();
				}
			}
		}
		
		if (nearest != null) {
			resolveFaceCollision(bsp, nearest, nearestFace);
			faces.remove(nearestFace);
			
		} else {
			if (!collidedWithMaterial && lastFloor != null) {
				checkContactMaterial(lastFloor);
			}
			return;
		}
		
		if (iterations != 5) {
			handleFaceCollisions(bsp, faces, iterations + 1);
			return;
		}
		
		if (!collidedWithMaterial && lastFloor != null) {
			checkContactMaterial(lastFloor);
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
				final int numPlanes = clip.planes.length;
				
				if (arc.getActiveTrigger(this) == clip)
					continue;
				
				Plane[] planes = new Plane[numPlanes];
				
				for(int i = 0; i < numPlanes; i++)
					planes[i] = bsp.planes[clip.planes[i]];
				
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
		// TODO: THIS
		/*if (mtv.getAxis().y < .5f) {
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
		
		if (Debug.showCollisions && mtv != null) {
			LineRender.drawTriangle(tri, Colors.RED);
		}
		
		if (mtv.getAxis().y >= .5f) {
			collideWithFloor(tri.getPlane());
			return;
		}
		
		pos.add(mtv.getMTV());
		vel.add(Vectors.div(mtv.getMTV(), Window.deltaTime));*/
	}
	
	private void resolveFaceCollision(Bsp bsp, MTV mtv, ArcFace face) {
		if (face == null) {
			position.add(mtv.getMTV());
			return;
		}
		
		if (Debug.showCollisions && mtv != null) {
			for(int i = face.firstEdge; i < face.numEdges + face.firstEdge; i++) {
				int edgeId = bsp.surfEdges[i];
				ArcEdge edge = bsp.edges[Math.abs(edgeId)];
				Vector3f e1 = new Vector3f(bsp.vertices[edge.start]);
				Vector3f e2 = new Vector3f(bsp.vertices[edge.end]);
				
				if (edgeId > 0) {
					Vector3f t = new Vector3f(e1);
					e1.set(e2);
					e2.set(t);
				}
				
				LineRender.drawLine(e1, e2, Colors.RED);
				
				Vector3f center = new Vector3f(e2).sub(e1).div(2f).add(e1);
				Plane plane = bsp.planes[face.planeId];
				Vector3f normal = new Vector3f(e2).sub(e1).normalize().cross(plane.normal);
				LineRender.drawLine(center, new Vector3f(center).add(normal), Colors.GREEN);
			}
		}
		
		Plane plane = bsp.planes[mtv.getFace().planeId];

		// If floor..
		// if (plane.normal.y >= .05f || bbox.Z.y > VERT_TRANSITION_THRESHOLD) {
		if (plane.normal.dot(bbox.Y) >= .6f || bbox.Z.y > VERT_TRANSITION_THRESHOLD) {

			if (vertAxis != null && vel.y > 0f)
				return;
			
			boolean GRIND = Input.isDown("tr_grind");
			if (!GRIND && speed > 20f && bbox.Y.y < .32f && lastPos.y < position.y - .3f && plane.normal.dot(bbox.Y) >= .8f) {
				launchOffRamp(plane);
				return;
			}
			
			vertExitSpeed = Float.NaN;
			vertAxis = null;
			vertExitDir = Float.NaN;
			
			Vector3f castDir = new Vector3f(bbox.Y).negate();
			float toGround = CollideUtils.raycastMapGeometry(bsp, face, bbox.getCenter(), castDir);

			if (Float.isNaN(toGround) && grindRail == null)
				return;
			
			if (vel.y > 0f && plane.normal.y < .5f && toGround > bbox.getHeight() + .5f)
				return;
			
			toGround = plane.raycast(bbox.getCenter(), new Vector3f(bbox.Y).negate());
			
			if (Float.isNaN(toGround)) {
				position.add(mtv.getMTV());
				return;
			}
			
			final Plane p = bsp.planes[face.planeId];
			
			lastFloor = face;
			grounded = true;

			position.y -= toGround;
			position.y += bbox.getHeight();
			
			if (grindRail == null) {
				Quaternionf q = new Quaternionf();
				q.rotationTo(0, 1, 0, p.normal.x, p.normal.y, p.normal.z);
				q.rotateY(-direction);
				rotation.set(q);
				bbox.setRotation(q);

				if (plane.normal.y < .99f) {
					Quaternionf diff = new Quaternionf().rotationTo(bbox.Y, plane.normal);
					vel.rotate(diff);
				}
			}
		} else {
			bbox.center.y += WALL_FORGIVENESS;
			MTV wallHopMtv = CollideUtils.faceCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, plane.normal,
					bbox);
			bbox.center.y -= WALL_FORGIVENESS;

			if (wallHopMtv == null) {
				return;
			}

			if (grindRail != null)
				endGrind();

			// Handle velocity depending on direction to wall
			float facing = vel.dot(new Vector3f(plane.normal.z, 0f, -plane.normal.x)) > 0 ? MathUtil.PI : 0f;
			position.add(mtv.getMTV());

			float newDirection = (float) Math.atan2(plane.normal.z, plane.normal.x) + facing;
			float vy = vel.y;
			
			if (!grounded || Math.abs(direction - newDirection) < MathUtil.HALFPI/2f) {
				float vLen = new Vector3f(vel.x, 0f, vel.z).length();
				vel.set(plane.normal.z, 0f, -plane.normal.x);
				
				if (facing == 0f)
					vel.negate();
				
				vel.mul(vLen);
			} else {
				vel.zero();
			}

			vel.y = vy;
			direction = newDirection;
			position.add(Vectors.mul(mtv.getAxis(), bbox.getWidth() * 0.16f));
		}

		this.updatePositions();
		return;
	}

	private void launchOffRamp(Plane plane) {
		vel.set(plane.normal);
		vel.y += 80f;
		position.y += 1f;
		position.x += plane.normal.x;
		position.z += plane.normal.z;
		grounded = false;
		previouslyGrounded = false;
		vertExitSpeed = vel.y;
		vertExitDir = direction;
		
		vertAxis = new Vector3f(plane.normal);
		float dx = vertAxis.x;
		vertAxis.set(vertAxis.z, 0f, dx);
		vertAxis.normalize();
	}

	protected void collideWithFloor(Plane plane) {
		grounded = true;
	}
	
	public Vector3f getGrindStart() {
		return grindStart;
	}
	
	/**
	 * Applies a force to this entity
	 * 
	 * @param direction the direction of the force
	 * @param magnitude the magnitude of the force
	 */
	public void accelerate(Vector3f direction, float magnitude) {
		final float projVel = vel.dot(direction); // Vector projection of Current vel onto accelDir.
		float accelVel = magnitude * Window.deltaTime; // Accelerated vel in direction of movment

		// If necessary, truncate the accelerated vel so the vector projection does
		// not exceed max_vel
		float speedCap = maxAirSpeed;
		
		if (grounded) {
			float factor = (1f - bbox.Y.y) * speedRamp * Math.signum(vel.y);
			speedCap = maxSpeed - factor;
		}

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
	
	protected void checkContactMaterial(ArcFace face) {
		int id = arc.bsp.getTextureMappings()[face.texMapping].textureId;
		Material newMaterial = arc.getTextures()[id].getMaterial();
		
		checkContactMaterial(newMaterial);
	}
	
	private void checkContactMaterial(Material newMaterial) {
		if (newMaterial == contactMaterial && rideSfxPlaying)
			return;
		
		contactMaterial = newMaterial;
		
		determineRideSound();
	}
	
	private void determineRideSound() {
		
		if (contactMaterial == null)
			return;

		String sfx;
		
		switch(contactMaterial) {
		case GRASS:
		case MUD:
			sfx = "ride_grass";
			break;
		case WOOD:
			sfx = "ride_grass";
		default:
			sfx = "ride_rock";
			break;
		}
		
		if (grindRail == null) {
			rideSfxSource.play(sfx);

			rideSfxSource.setGain(0f);
			rideSfxPlaying = true;
		}
	}
	
	public Material getContactMaterial() {
		return contactMaterial;
	}

	public boolean isGrounded() {
		return grounded;
	}

	public boolean isSliding() {
		return sliding;
	}
	
	public BoundingBox getBBox() {
		return bbox;
	}
	
	public void setGrindLen(float grindLen) {
		this.grindLen = grindLen;
	}
	
	public float getGrindLen() {
		return grindLen;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		trickSfxSource.delete();
		rideSfxSource.delete();
	}
}
