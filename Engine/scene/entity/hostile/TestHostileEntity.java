package scene.entity.hostile;

import org.joml.Vector3f;

import core.Application;
import gl.res.Model;
import map.architecture.components.ArcNavigation;
import scene.PlayableScene;
import scene.entity.EntityHandler;
import scene.entity.utility.NavigableEntity;
import scene.entity.utility.PlayerEntity;

public class TestHostileEntity extends NavigableEntity {
	private PlayerEntity player;
	
	public TestHostileEntity(PlayerEntity player) {
		super("test_monster", new Vector3f(4f, 10f, 4f));
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
			player.takeDamage(5, 0);
			player.takeDamage(2, 1);
			player.takeDamage(2, 2);
		}
	}
	
	public static String spawnViaCommand(float x, float y, float z) {
		if (!(Application.scene instanceof PlayableScene)) {
			return "must be in playable scene";
		}
		
		PlayableScene scene = (PlayableScene)Application.scene;
		ArcNavigation navigation = scene.getArcHandler().getArchitecture().getNavigation();
		
		//Vector3f pos = Application.scene.getCamera().getPosition();
		TestHostileEntity entity = new TestHostileEntity(scene.getPlayer());
		entity.pos.set(x,y,z);
		entity.initNavigation(navigation);
		entity.setTarget(scene.getPlayer().pos);
		EntityHandler.addEntity(entity);
		
		return null;
	}
}
