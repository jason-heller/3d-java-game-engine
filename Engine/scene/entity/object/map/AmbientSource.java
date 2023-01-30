package scene.entity.object.map;

import org.joml.Vector3f;

import audio.AudioHandler;
import audio.Source;
import scene.PlayableScene;
import scene.entity.Entity;
import util.Vectors;

public class AmbientSource extends Entity {

	private String sfx;
	private float volume, dist;
	
	private boolean playing = false;
	
	private Source source;
	
	public AmbientSource(String name, Vector3f pos, String sfx, float volume, float dist) {
		super(name);
		this.position = pos;
		this.sfx = sfx;
		this.volume = volume;
		this.dist = dist;
		
		source = AudioHandler.checkoutSource();
		
		source.setLooping(true);
		source.setPosition(pos);
	}
	
	@Override
	public void update(PlayableScene scene) {
		float toCamSqr = Vectors.distanceSquared(position, scene.getCamera().getPosition());
		if (toCamSqr < dist * dist) {

			if (!playing) {
				playing = true;
				source.play(sfx);
			}
			
			float toCam = (float)Math.sqrt(toCamSqr);
			float halfDist = dist/2f;
			// TODO: Linear distance sucks
			source.setGain(volume * (1f - Math.max((toCam - halfDist) / halfDist, 0f)));
			
		} else if (playing) {
			source.stop();
			playing = false;
		}
	}
	
	/*@Override
	public void cleanUp() {
		source.delete();
	}*/

}
