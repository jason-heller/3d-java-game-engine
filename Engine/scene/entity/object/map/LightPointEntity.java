package scene.entity.object.map;

import java.util.List;

import org.joml.Vector3f;

import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.util.LightStyle;

public class LightPointEntity extends Entity {
	
	private float linear, quadratic;
	private Vector3f color;
	private LightStyle style;
	
	public LightPointEntity(Vector3f pos, LightStyle style, float linear, float quadratic, Vector3f color) {
		super("light");
		this.pos = pos;
		this.color = color;
		this.style = style;
		this.linear = linear;
		this.quadratic = quadratic;
		/*faces = new int[facesStr.length / 2];
		for(int i = 0; i < faces.length; i++) {
			faces[i] = (int)facesStr[i*2] + ((int)facesStr[i*2 + 1] << 8);
		}*/
	}



	@Override
	public void update(PlayableScene scene) {
		
		// TODO: Light up entities within its area of influence
		
		if (style == LightStyle.STATIC)
			return;
		
		final String brightnessPattern;

		switch(style.getLightCondition()) {
		default:
			brightnessPattern = style.getBrightnessPattern(0);
		}
		
		
		final int duration = brightnessPattern.length() * LightStyle.ANIMATION_SPEED_MS;
		final int animationIndex = ((int)(System.currentTimeMillis() % duration)) / LightStyle.ANIMATION_SPEED_MS;
		
		float alpha = (brightnessPattern.charAt(animationIndex) - 97) / 26f;	// 97 = 'a' in ascii
		
		updateFaceLightAlpha(alpha, scene.getArchitecture().getActiveLeaves().getNear());
	}
	
	private void updateFaceLightAlpha(float brightness, List<BspLeaf> renderedLeaves) {
		for(BspLeaf leaf : renderedLeaves) {
			Vector3f center = Vector3f.add(leaf.max, leaf.min).mul(0.5f);
			// FIXME: 400*400 is a hack, should really determine its actual reach
			if (Vector3f.distanceSquared(center, pos) < 400*400) {
				leaf.setAlpha(style, brightness);
			}
		}
	}
}
