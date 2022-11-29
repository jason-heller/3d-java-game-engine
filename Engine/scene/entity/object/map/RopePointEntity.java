package scene.entity.object.map;

import org.joml.Vector3f;

import gl.Window;
import gl.line.LineRender;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;

public class RopePointEntity extends Entity {
	
	private RopePointEntity next = null;
	private Vector3f color;
	private float give;
	private final int segments;
	private String nextName = "";
	private float sway = 0f;
	private float swaySpeed = 1f;
	private float swayScale;

	public RopePointEntity(Vector3f pos, String name, String nextName, float give, int precision, Vector3f color, float swaySpeed) {
		super(name);
		this.pos = pos;
		this.give = give;
		this.color = color;
		this.segments = 8 * precision;		// Maybe determine this on linear distance between nodes?
		this.nextName = nextName;
		this.swaySpeed = swaySpeed;
		this.swayScale = 0f;//1f / 8f;
		
		// FIXME: This makes everything go to shit
		//this.deactivationRange = 0f;
		
		sway = (float)(Math.random() * Math.PI * 2.0);
	}

	@Override
	public void update(PlayableScene scene) {
		// No need to call super.update(), since this will render with lines
		if (this.next != null) {
			sway += Window.deltaTime * swaySpeed;
			float swayVal = (float)Math.sin(sway) * swayScale;
			
			Vector3f point = new Vector3f(pos);
			Vector3f posInc = Vector3f.sub(next.pos, pos).div(segments);
			Vector3f swayNormal = Vector3f.cross(posInc, Vector3f.Y_AXIS);
			Vector3f swayOffset = Vector3f.mul(swayNormal, swayVal);
			Vector3f sway = new Vector3f();

			final float parabInc = 1f / segments;
			float drop = 0f;
			
			for (int i = 1; i <= segments; i++) {

				Vector3f curPointWithSlack = new Vector3f(point);
				curPointWithSlack.y += drop;
				curPointWithSlack.add(sway);
				
				point.add(posInc);
				float x = (2f * (i * parabInc)) - 1f;
				float parabX = (x * x) - 1f;
				drop = parabX * give;
				sway.set(Vector3f.mul(swayOffset, parabX));

				Vector3f nextPointWithSlack = new Vector3f(point);
				nextPointWithSlack.y += drop;
				nextPointWithSlack.add(sway);
				
				LineRender.drawLine(curPointWithSlack, nextPointWithSlack, color);
				
			}
		} else if (!nextName.equals("")) {
			this.next = (RopePointEntity) EntityHandler.getEntity(nextName);
			if (this.next != null)
				this.swayScale = 1f / (Vector3f.distance(pos, this.next.pos) / 8f);
			// Assumes the parent is a RopePointEntity, probably will crash if it's not so don't fuck this up
			nextName = null;
		}
		
	}
}
