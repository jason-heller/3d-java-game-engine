package scene.entity.object;

import org.joml.Vector3f;

import audio.AudioHandler;
import geom.AxisAlignedBBox;
import gl.Window;
import gl.line.LineRender;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.util.PhysicsEntity;
import util.Colors;
import util.MathUtil;

public class MotionSensorEntity extends Entity {

	private float activationTimer = 2.625f;
	private Vector3f normal, rayEndPt;
	
	public MotionSensorEntity(Vector3f pos, Vector3f normal) {
		super("sensor");
		this.pos = pos;
		this.normal = normal;
		if (normal.y == 1.0f || normal.y == -1.0f) {
			this.rot.set(0, 0, 90 * normal.y);
		} else {
			this.rot = MathUtil.directionVectorToEuler(normal, Vector3f.Y_AXIS);
		}
		this.setModel("cube");
		this.setTexture("default");
		AudioHandler.play("place");
		
		rayEndPt = Vector3f.add(pos, Vector3f.mul(normal, 5f));
	}

	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		
		if (activationTimer != 0f) {
			activationTimer -= Window.deltaTime;
			
			if (activationTimer <= 0f) {
				activationTimer = 0f;
			}
		} else {
			LineRender.drawLine(pos, rayEndPt, Colors.RED);
			
			for(Entity entity : EntityHandler.getEntities(this.leaf)) {
				if (!(entity instanceof PhysicsEntity))
					continue;
				
				PhysicsEntity physEnt = (PhysicsEntity)entity;
				AxisAlignedBBox bbox = physEnt.getBBox();
				if (bbox.collide(pos, normal) > 6f)
					continue;
				
				if (physEnt.vel.lengthSquared() > .1f) {
					activationTimer = 4f;
				}
			}
		}
	}
}
