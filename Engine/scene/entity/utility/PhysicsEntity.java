package scene.entity.utility;


import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import core.Application;
import core.Resources;
import dev.Debug;
import geom.AxisAlignedBBox;
import geom.CollideUtils;
import geom.MTV;
import geom.Plane;
import gl.Window;
import gl.line.LineRender;
import gl.res.Texture;
import map.architecture.Architecture;
import map.architecture.ArchitectureHandler;
import map.architecture.Material;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcTriggerClip;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.ground.TerrainSampler;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.overworld.Overworld;
import util.Colors;

public abstract class PhysicsEntity extends Entity {
	
	private static final float DEFAULT_FRICTION = 3f;
	
	protected boolean grounded = false;
	public boolean previouslyGrounded = false;
	protected boolean sliding = false;
	protected boolean submerged = false;

	private boolean fullySubmerged = false;
	private boolean climbing = false;
	
	public static float gravity = 25f;
	public static float maxGravity = -200f;
	
	protected Material materialStandingOn = Material.ROCK;
	
	public float maxSpeed = 25f, maxAirSpeed = 5f, maxWaterSpeed = 1f;
	public float friction = DEFAULT_FRICTION;
	public float airFriction = 0f;

	public boolean visible = true;
	
	private boolean collideWithTerrain;

	protected static final float SLIDE_ANGLE = .9f;
	protected static final float EPSILON = 0.01f;

	protected AxisAlignedBBox bbox;
	public Vector3f vel = new Vector3f();
	
	private ArchitectureHandler arcHandler;
	
	public PhysicsEntity(String name, Vector3f bounds) {
		super(name);
		bbox = new AxisAlignedBBox(pos, bounds);
		collideWithTerrain = (Application.scene instanceof Overworld);
		arcHandler = ((PlayableScene)Application.scene).getArcHandler();
	}

	public void accelerate(Vector3f dir, float amount) {
		if (climbing) {
			vel.y += amount * Window.deltaTime;

			vel.x += dir.x * amount * Window.deltaTime;
			vel.z += dir.z * amount * Window.deltaTime;
		} else {
			final float projVel = Vector3f.dot(vel, dir); // Vector projection of Current vel onto accelDir.
			float accelVel = amount * Window.deltaTime; // Accelerated vel in direction of movment

			// If necessary, truncate the accelerated vel so the vector projection does
			// not exceed max_vel
			final float speedCap;
			if (submerged) {
				speedCap = maxWaterSpeed;
			} else {
				speedCap = grounded ? maxSpeed : maxAirSpeed;
			}
			// if (projVel + accelVel < -speedCap)
			// accelVel = -speedCap - projVel;

			if (projVel + accelVel > speedCap) {
				accelVel = speedCap - projVel;
			}

			vel.x += dir.x * accelVel;
			vel.y += dir.y * accelVel;
			vel.z += dir.z * accelVel;
		}
	}

	public boolean isGrounded() {
		return grounded;
	}

	public boolean isSliding() {
		return sliding;
	}

	public boolean isSubmerged() {
		return submerged;
	}
	
	public boolean isFullySubmerged() {
		return fullySubmerged;
	}
	
	public boolean isClimbing() {
		return climbing;
	}

	public void jump(float height) {
		if (climbing) {
			vel.x = -vel.x;
			vel.z = -vel.z;
			vel.y = height;
			climbing = false;
			grounded = false;
			sliding = false;

			//previouslyGrounded = false;
		} else {
			vel.y = height;
			grounded = false;
			sliding = false;
			//previouslyGrounded = false;
		}
	}

	@Override
	public void update(PlayableScene scene) {
		if (Debug.showHitboxes) {
			LineRender.drawBox(bbox.getCenter(), bbox.getBounds(), Colors.YELLOW);
		}
		
		//aabb.setCenter(pos.x, pos.y, pos.z);
		if (!submerged && !climbing) {
			vel.y = Math.max(vel.y - gravity * Window.deltaTime, maxGravity);
		}

		if (!climbing) {
			pos.x += vel.x * Window.deltaTime;
			pos.z += vel.z * Window.deltaTime;
		}

		pos.y += vel.y * Window.deltaTime;

		// Friction
		if (!sliding && previouslyGrounded || submerged) {
			final float speed = vel.length();
			if (speed != 0) {
				float drop = speed * friction * Window.deltaTime;
				if (submerged) {
					drop /= 2;
					grounded = false;
				}
				final float offset = Math.max(speed - drop, 0) / speed;
				vel.mul(offset); // Scale the vel based on friction.
			}
		} else if (climbing) {
			final float speed = Math.abs(vel.y);
			if (speed != 0) {
				final float drop = speed * friction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				vel.y *= offset;
				vel.x = Math.signum(vel.x) * vel.y;
				vel.z = Math.signum(vel.z) * vel.y;
			}
		}

		else if (airFriction != 0f && !sliding && !submerged) {
			final float speed = new Vector2f(vel.x, vel.z).length();
			if (speed != 0f) {
				final float drop = speed * airFriction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				vel.set(vel.x * offset, vel.y, vel.z * offset); // Scale the vel based on
																					// friction.
			}
		}

		previouslyGrounded = grounded;

		if (collideWithTerrain)
			terrainCollide();
		else
			grounded = false;
		
		climbing = false;
		
		Architecture architecture = arcHandler.getArchitecture();
		Bsp bsp = architecture.bsp;
		Vector3f max = Vector3f.add(bbox.getCenter(), bbox.getBounds());
		Vector3f min = Vector3f.sub(bbox.getCenter(), bbox.getBounds());
		
		List<BspLeaf> leaves = bsp.walk(max, min);
		List<ArcFace> faces = bsp.getFaces(leaves);
		
		mapGeometryCollide(bsp, faces);
		mapClipCollide(architecture, leaves);
		
		super.update(scene);
	}

	private void mapClipCollide(Architecture arc, List<BspLeaf> leaves) {
		Bsp bsp = arc.bsp;
		for(BspLeaf leaf : bsp.leaves) {
			for(short clipId : leaf.clips) {
				ArcClip clip = bsp.clips[clipId];
				
				if (arc.getActiveTrigger(this) == clip)
					continue;
				
				Plane[] planes = new Plane[clip.numPlanes];
				for(int i = 0; i < clip.numPlanes; i++) {
					planes[i] = bsp.planes[clip.firstPlane + i];
				}
				
				MTV mtv = bbox.collide(clip.bbox);
				//MTV mtv = CollideUtils.convexHullBoxCollide(planes, bbox);
				if (mtv != null) {
					boolean doCollide = clip.interact(this, true);
					if (clip instanceof ArcTriggerClip) {
						arc.setTriggerActive(this, (ArcTriggerClip) clip);
					}
					
					if (doCollide) {
						collide(mtv);
					}
				}
			}
		}
	}

	private void terrainCollide() {
		float terrainY = TerrainSampler.barycentric(pos.x, pos.z);
		
		if (pos.y <= terrainY) {
			pos.y = terrainY;
			grounded = true;
		} else {
			grounded = false;
		}
	}
	
	private void mapGeometryCollide(Bsp bsp, List<ArcFace> faces) {
		if (previouslyGrounded  && !grounded) {
			floorStickGeomCol(bsp, faces);
			mapGeometryCollide(bsp, faces, 1);
			return;
		}
		
		mapGeometryCollide(bsp, faces, 0);
	}
	
	private void mapGeometryCollide(Bsp bsp, List<ArcFace> faces, int iterations) {
		MTV nearest = null;
		ArcFace nearestFace = null;

		for(ArcFace face : faces) {
			
			Plane plane = bsp.planes[face.planeId];
			Vector3f normal = plane.normal;
			
			MTV mtv = CollideUtils.bspFaceBoxCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, normal, bbox);
			
			if (Debug.viewCollide && mtv != null) {
				for(int i = face.firstEdge; i < face.numEdges + face.firstEdge; i++) {
					ArcEdge edge = bsp.edges[Math.abs(bsp.surfEdges[i])];
					LineRender.drawLine(bsp.vertices[edge.start], bsp.vertices[edge.end], new Vector3f(0,0,1f));
				}
			}
			
			if (mtv != null && (nearest == null || nearest.compareTo(mtv) >= 0)) {
				nearest = mtv;
				nearestFace = face;
			}
		}
		
		if (nearest != null) {
			MTV mtv = nearest;
			
			collide(mtv/*, bsp.planes[nearestFace.planeId]*/);
			faces.remove(nearestFace);
			
		} else {
			return;
		}
		
		if (iterations != 7) {	// Only allow up to 8 iterations
			mapGeometryCollide(bsp, faces, iterations + 1);
		}
	}

	private void floorStickGeomCol(Bsp bsp, List<ArcFace> faces) {
		MTV nearest = null;
		ArcFace nearestFace = null;

		for(ArcFace face : faces) {
			Plane plane = bsp.planes[face.planeId];
			Vector3f normal = plane.normal;
			if (normal.y < 0.75f)
				continue;
			
			MTV mtv = CollideUtils.bspFaceBoxCollide(bsp.vertices, bsp.edges, bsp.surfEdges, face, normal, bbox);
			
			if (Debug.viewCollide && mtv != null) {
				for(int i = face.firstEdge; i < face.numEdges + face.firstEdge; i++) {
					ArcEdge edge = bsp.edges[Math.abs(bsp.surfEdges[i])];
					LineRender.drawLine(bsp.vertices[edge.start], bsp.vertices[edge.end], new Vector3f(0,0,1f));
				}
			}
			
			if (mtv != null && (nearest == null || nearest.compareTo(mtv) >= 0)) {
				nearest = mtv;
				nearestFace = face;
				Architecture arc = arcHandler.getArchitecture();

				int id = arc.getTexData()[face.texId].textureId;
				if (id != -1) {
					String texName = arc.getMapTextureRefs()[id];
					Texture tex = Resources.getTexture(texName);
					
					materialStandingOn = tex.getMaterial();
				}
			}
		}
		
		if (nearest != null) {
			MTV mtv = nearest;
			collideWithFloor(mtv);
			
			faces.remove(nearestFace);
		} else
			return;
	}
	
	private void collide(MTV mtv) {
		if (mtv.getPlane().normal.y > .5f) {
			collideWithFloor(mtv);
			/*Vector3f tempVel = new Vector3f(vel);
			vel.y = 0f;
			pos.add(mtv.getMTV());
			vel.x = tempVel.x;
			vel.z = tempVel.z;*/
			return;
		}
		
		if (mtv.getDepth() > 0.01f) {
			Plane plane = mtv.getPlane();
			Vector3f projVel = plane.projectPoint(Vector3f.add(pos, vel));
			Vector3f projPos = plane.projectPoint(pos);
			vel.set(Vector3f.sub(projVel, projPos));
		}
		
		pos.add(mtv.getMTV());
	}

	protected void collideWithFloor(MTV mtv) {
		grounded = true;
		
		// Easy out
		if (mtv.getAxis().y == 1f) {
			pos.y += mtv.getMTV().y;
			vel.y = 0f;
			return;
		}
		
		vel.y = mtv.getAxis().y < .99f ? -6f: 0f;
		
		Plane plane = mtv.getPlane();
		float depth = Float.NEGATIVE_INFINITY;
		
		final Vector3f[] points = new Vector3f[] {
				new Vector3f(bbox.getBounds().x, -bbox.getBounds().y, bbox.getBounds().z),
				new Vector3f(bbox.getBounds().x, -bbox.getBounds().y, -bbox.getBounds().z),
				new Vector3f(-bbox.getBounds().x, -bbox.getBounds().y, bbox.getBounds().z),
				new Vector3f(-bbox.getBounds().x, -bbox.getBounds().y, -bbox.getBounds().z)
				};
		
		for(int i = 0; i < 4; i++) {
			float newDepth = plane.collide(Vector3f.add(pos, points[i]), Vector3f.Y_AXIS);

			if (!  Float.isNaN(newDepth) && newDepth > depth) 
				depth = newDepth;
		}
		
		if (depth == Float.NEGATIVE_INFINITY) {
			return;
		}

		pos.y += depth;
	}

	public AxisAlignedBBox getBBox() {
		return bbox;
	}
	
	public Material getMaterialStandingOn() {
		return materialStandingOn;
	}

	public void setClimbing(boolean climbing) {
		this.climbing = climbing;
	}
}
