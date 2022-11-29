package scene.entity.util;


import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import core.App;
import dev.Debug;
import dev.cmd.Console;
import geom.AxisAlignedBBox;
import geom.CollideUtils;
import geom.MTV;
import geom.Plane;
import geom.Polygon;
import gl.Window;
import gl.line.LineRender;
import gl.res.Mesh;
import map.architecture.Architecture;
import map.architecture.ArchitectureHandler;
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
import util.MathUtil;

public abstract class SkatePhysicsEntity extends Entity {
	
	protected boolean grounded = false;
	public boolean previouslyGrounded = false;
	protected boolean sliding = false;
	
	public static float gravity = 100f;
	public static float maxGravity = -600f;
	public static float friction = 6f;
	public static float airFriction = .1f;
	
	protected boolean applyGravity = true;
	
	protected Material materialStandingOn = Material.ROCK;
	
	public float maxSpeed = 25f, maxAirSpeed = 5f, maxWaterSpeed = 1f;

	protected static final float SLIDE_ANGLE = .9f;
	protected static final float EPSILON = 0.01f;
	private static final float STEP_HEIGHT = 3f;

	public Vector3f vel = new Vector3f();
	private Vector3f lastPos = new Vector3f();
	
	protected ArchitectureHandler arcHandler;
	
	public boolean solid = true;
	
	public SkatePhysicsEntity(String name, Vector3f bounds) {
		super(name);
		bbox = new AxisAlignedBBox(pos, bounds);
		arcHandler = ((PlayableScene)App.scene).getArcHandler();
	}

	public void jump(float height) {
		vel.y = height;
		grounded = false;
		sliding = false;
	}

	@Override
	public void update(PlayableScene scene) {
		if (Debug.showHitboxes) {
			LineRender.drawBox(bbox.getCenter(), bbox.getBounds(), Colors.YELLOW);
		}
		
		lastPos.set(pos);
		
		// Add vertical speed
		if (applyGravity) {
			vel.y = Math.max(vel.y - gravity * Window.deltaTime, maxGravity);
		}

		// Add speed
		pos.x += vel.x * Window.deltaTime;
		pos.y += vel.y * Window.deltaTime;
		pos.z += vel.z * Window.deltaTime;

		applyFriction();

		previouslyGrounded = grounded;
		grounded = false;
		
		Architecture architecture = arcHandler.getArchitecture();
		Bsp bsp = architecture.bsp;
		
		Vector3f max = Vector3f.add(bbox.getCenter(), bbox.getBounds());
		Vector3f min = Vector3f.sub(bbox.getCenter(), bbox.getBounds());
		
		List<BspLeaf> leaves = bsp.walk(max, min);
		List<ArcFace> faces = bsp.getFaces(leaves);
		
		leaf = bsp.walk(pos);
		
		// Main collision handling

		handleEntityCollisions(leaves);
		handleObjectCollisions(leaf, bsp);
		handleHeightmapCollisions(architecture, leaves, faces);
		handleFaceCollisions(bsp, faces, 0);
		handleClipCollisions(architecture, leaves);
		
		super.update(scene);
	}

	private void handleHeightmapCollisions(Architecture arc, List<BspLeaf> leaves, List<ArcFace> faces) {
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

	/** Handles collisions between this entity and other entities
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

					handleBBoxCollision(physEnt.bbox);
				}
			}
		}
	}
	
	private void handleBBoxCollision(AxisAlignedBBox other) {
		MTV mtv = bbox.collide(other);

		if (mtv != null) {
			if (mtv.getAxis().y > .5f) {
				vel.y = 0f;
				grounded = true;
				pos.y = other.getCenter().y + bbox.getBounds().y + other.getBounds().y;
			} else {
				vel.add(mtv.getMTV());
			}
		}
	}

	protected void handleObjectCollisions(BspLeaf leaf, Bsp bsp) {
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
					handleBBoxCollision(otherBox);
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

		for(ArcFace face : faces) {
			
			//Architecture arc = arcHandler.getArchitecture();
			if (face.texMapping == -1) 
				continue;
			
			Plane plane = bsp.planes[face.planeId];
			Vector3f normal = plane.normal;
			
			MTV mtv = CollideUtils.bspFaceBoxCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, normal, bbox);
			
			if (mtv != null && (nearest == null || nearest.compareTo(mtv) >= 0)) {
				nearest = mtv;
				nearestFace = face;
			}
		}
		
		if (nearest != null) {
			resolveFaceCollision(bsp, nearest, nearestFace);
			faces.remove(nearestFace);
			
		} else {
			return;
		}
		
		if (iterations != 7) {	// Only allow up to 8 iterations
			handleFaceCollisions(bsp, faces, iterations + 1);
		}
	}
	
	/** Handles collisions between this entity and clips
	 * @param arc The current map (referred internally as an architecture)
	 * @param leaves The list of leaves this entity currently resides in
	 */
	private void handleClipCollisions(Architecture arc, List<BspLeaf> leaves) {
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
				
				//MTV mtv = bbox.collide(clip.bbox);
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
		// Check if moving the player's path up one resolves collision, if it does, move along that path, then drop down

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
			plane = bsp.planes[mtv.getFace().planeId];
		}
		
		// If floor..
		if (plane.normal.y >= .49f) {
			if (Float.isNaN(CollideUtils.convexPolygonRay(bsp, face, pos, new Vector3f(0,-1,0)))) {
				return;
			}
			
			collideWithFloor(plane);
			
			if (face != null) {
				Architecture arc = arcHandler.getArchitecture();
				int id = bsp.getTextureMappings()[face.texMapping].textureId;
				materialStandingOn = arc.getTextures()[id].getMaterial();
			}
			return;
		} else {
			// If a wall, & 
			pos.y += 2f;
			MTV wallHopMtv = CollideUtils.bspFaceBoxCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, plane.normal, bbox);
			pos.y -= 2f;
			
			if (wallHopMtv == null) {
				return;
			}
		}
		
		pos.add(mtv.getMTV());
		vel.add(Vector3f.div(mtv.getMTV(), Window.deltaTime));
	}

	protected void collideWithFloor(Plane plane) {
		
		// Easy out
		/*if (mtv.getAxis().y == 1f) {
			pos.y += mtv.getMTV().y;
			vel.y = 0f;
			grounded = true;
			return;
		}*/
		
		float oldDepth = plane.raycast(Vector3f.add(lastPos, Vector3f.ZERO), new Vector3f(0,-1,0));
		float depth = plane.raycast(Vector3f.add(pos, Vector3f.ZERO), new Vector3f(0,-1,0));

		if (depth == Float.NEGATIVE_INFINITY || depth > bbox.getHeight()) {
			return;
		}
		
		if (depth > bbox.getHeight() - .1f) {
			grounded = true;
			pos.y += bbox.getHeight() - depth;
			//vel.y = 0;
			return;
		}

		grounded = true;
		
		pos.y += bbox.getHeight() - depth;
		//vel.set(Vector3f.sub(pos, lastPos).div(Window.deltaTime));
		vel.y = (pos.y - lastPos.y) / Window.deltaTime;
		//if (vel.y > 0)
		//float newHeight = pos.y;
		//pos.set(lastPos);
		//pos.y = newHeight;
		/*if (depth > upwarp) {
			upwarp = depth;
			// lastFloorCollided = plane;
		}*/
	}
	
	/** Applies a force to this entity
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
	
	/** Applies friction the entity when called, should be called once per tick
	 * 
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
}
