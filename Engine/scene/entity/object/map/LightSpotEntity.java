package scene.entity.object.map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.cmd.Console;
import gl.line.LineRender;
import scene.PlayableScene;
import scene.entity.util.LightStyle;

public class LightSpotEntity extends LightPointEntity {

	public LightSpotEntity(Vector3f pos, Vector3f rot, LightStyle style, float linear, float quadratic, Vector3f color) {
		super(pos, style, linear, quadratic, color);
		this.rotation = new Quaternionf().rotateX(rot.x).rotateY(rot.y).rotateZ(rot.z);
		Console.warning(rot);
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		LineRender.drawPoint(position);
	}
}
