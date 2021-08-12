package scene.entity.hostile;

import org.joml.Vector3f;

import gl.Window;
import gl.res.Model;
import gl.res.Texture;
import scene.PlayableScene;
import scene.entity.Entity;
import scene.entity.EntityHandler;

public class ApparitionEntity extends Entity {
	private float life;
	
	public ApparitionEntity(Model model, Texture texture, Vector3f pos, Vector3f rot, float scale) {
		super("apparition");
		this.pos.set(pos);
		this.rot.set(rot);
		this.scale = scale;
		this.setModel(model);
		this.setTexture(texture);
		
		life = 5f;
	}
	
	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		
		life -= Window.deltaTime;
		
		if (life <= 0f) {
			EntityHandler.removeEntity(this);
		}
	}
}
