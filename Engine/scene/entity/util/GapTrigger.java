package scene.entity.util;

import org.joml.Vector3f;

import dev.Debug;
import dev.cmd.Console;
import geom.BoundingBox;
import geom.Plane;
import gl.line.LineRender;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;
import scene.PlayableScene;
import scene.entity.object.map.BrushEntity;
import scene.mapscene.trick.TrickType;
import util.Colors;
import util.Vectors;

public class GapTrigger extends BrushEntity {

	private long activationTime;
	
	private boolean requireGrounded;
	private boolean requireAirborne;
	private boolean requireGrinding;
	private boolean requireCombo;
	private boolean requireLanding;
	
	private boolean active;
	
	public GapTrigger(String name, int flags) {
		super(name);

		this.name = name;
		//deactivationRange = 0f;
		
		this.requireGrounded = (flags & 1) != 0;
		this.requireAirborne = (flags & 2) != 0;
		this.requireGrinding = (flags & 4) != 0;
		this.requireCombo = (flags & 8) != 0;
		this.requireLanding = (flags & 128) != 0;
		
		Console.log(requireLanding,flags);
	}


	@Override
	public void update(PlayableScene scene) {
		
		if (!active) {
			doCollide(scene);
		} else if (!conditionsAreMet(scene.getPlayer())) {
			active = false;
		}
		
		if (Debug.showClips) {
			for(int i = firstFace; i < lastFace; i++) {
				Bsp bsp = scene.getArchitecture().bsp;
				ArcFace face = bsp.faces[i];
				for(int j = face.firstEdge; j < face.numEdges + face.firstEdge; j++) {
					ArcEdge edge = bsp.edges[Math.abs(bsp.surfEdges[j])];
					Vector3f start = Vectors.add(bsp.vertices[edge.start], position);
					Vector3f end = Vectors.add(bsp.vertices[edge.end], position);
					LineRender.drawLine(start, end, active ? Colors.CYAN : Colors.ORANGE);
				}
			}
		}
		
		
	}
	
	private void doCollide(PlayableScene scene) {
		PlayerEntity player = scene.getPlayer();
		BoundingBox bbox = player.getBBox();
		
		for(int i = firstFace; i < lastFace; i++) {
			Bsp bsp = scene.getArchitecture().bsp;
			ArcFace face = bsp.faces[i];
			Plane plane = bsp.planes[face.planeId];
			
			if (plane.signedDistanceTo(Vectors.sub(player.position, position)) > getExtents(bbox, plane.normal) && !bbox.intersects(plane)) {
				active = false;
				return;
			}
		}
		
		active = conditionsAreMet(player);
		
		if (active)
			activationTime = System.currentTimeMillis();
	}

	private float getExtents(BoundingBox bbox, Vector3f normal) {
		float xz = Math.max(Math.abs(bbox.X.dot(normal)), Math.abs(bbox.Z.dot(normal)));
		float y = Math.abs(bbox.Y.dot(normal));
		
		return (xz > y) ? bbox.getWidth() : bbox.getHeight();
	}


	private boolean conditionsAreMet(PlayerEntity player) {
		boolean grounded = player.isGrounded();
		
		if (requireGrounded && grounded)
			return true;
		if (requireAirborne && !grounded)
			return true;
		
		// We want this gap to trigger even if the player is airborne at the start of the grind
		// The check to make sure a grind has happened will be in the gap entity itself
		if (requireGrinding && player.isComboing() && player.getTrickManager().comboContains(TrickType.GRIND_TRICK))
			return true;
		
		if (requireCombo && player.isComboing())
			return true;
		
		
		return false;
	}
	
	public long getActivationTime() {
		return activationTime;
	}

	public boolean isActive() {
		return active;
	}


	public boolean requiresGrind() {
		return requireGrinding;
	}

	public boolean requiresLanding() {
		return requireLanding;
	}

	public void setActive(boolean b) {
		this.active = false;
	}
}
