package scene.entity.hostile;

import org.joml.Vector3f;

import core.Application;
import map.architecture.components.ArcNavigation;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.Spawnable;
import scene.entity.util.NavigableEntity;
import scene.entity.util.PlayerEntity;

public class TestHostileEntity extends NavigableEntity implements Spawnable {
	private PlayerEntity player;
	
	public TestHostileEntity() {
		super("test_monster", new Vector3f(3f, 6f, 3f));
		this.setModel(EntityHandler.billboard);
		this.setTextureUnique("entity01_test", "entity/clown.png");
		scale = 5;
		speed = 80;
	}
	
	public TestHostileEntity(PlayerEntity player) {
		super("test_monster", new Vector3f(2f, 6f,2f));
		this.player = player;
		this.setModel(EntityHandler.billboard);
		this.setTextureUnique("entity01_test", "entity/clown.png");
		scale = 5;
		speed = 80;
		
	}

	@Override
	public void update(PlayableScene scene) {
		super.update(scene);
		if (player.getBBox().intersects(bbox)) {
			player.takeDamage(5);
		}
	}

	@Override
	public boolean spawn(Vector3f pos, Vector3f rot, String... args) {
		PlayableScene scene = (PlayableScene)Application.scene;
		ArcNavigation navigation = scene.getArchitecture().getNavigation();
		
		player = scene.getPlayer();
		this.pos.set(pos);
		setNavigation(navigation);
		setTarget(scene.getPlayer().pos);
		return true;
	}
}
