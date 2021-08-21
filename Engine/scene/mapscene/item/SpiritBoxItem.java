package scene.mapscene.item;

import org.joml.Vector3f;

import audio.recognition.Speech;
import dev.cmd.Console;
import scene.entity.hostile.GhostEntity;
import scene.mapscene.MapScene;
import scene.viewmodel.ViewModel;
import scene.viewmodel.ViewModelSpiritBox;

public class SpiritBoxItem extends Item {
	
	private static final float RANGE_SQR = 50*50;
	
	public SpiritBoxItem(MapScene scene) {
		super(scene);
	}

	@Override
	public void interact() {
		
	}
	
	@Override
	public void interactEnd() {
	}
	
	@Override
	public void update() {
		GhostEntity ghost = scene.getGhost();
		float distToPlayerSqr = Vector3f.distanceSquared(ghost.pos, scene.getPlayer().pos);
		if (distToPlayerSqr <= RANGE_SQR) {// && ghost.raycastToPlayer(scene.getArchitecture())
			String last = Speech.getResultText();
			if (!last.equals("<unk>")) {
				Console.log("Heard: "+ last);
			}
			
			processVoice(ghost, last);
			Speech.clearResultText();
		}
	}

	private void processVoice(GhostEntity ghost, String said) {
		switch(said) {
		case "are you here":
		case "are you there":
		case "are you dead":
		case "are you a ghost":
		case "are you a spirit":
		case "is this place haunted":
			// I guess yes is the only answer
			ghost.aggression++;
			break;
		case "where are you":
			ghost.aggression++;
			break;
		case "where did you die":
			ghost.aggression++;
			break;
		case "show yourself":
		case "go to hell":
		case "fuck you":
		case "reveal yourself":
		case "go fuck yourself ":
			ghost.aggression+=3;
			break;
		}
	}

	@Override
	public ViewModel setViewModel() {
		return new ViewModelSpiritBox();
	}

	@Override
	public boolean setHoldKeyToInteract() {
		return false;
	}

	@Override
	public void equip() {
		source.setLooping(true);
		source.play("spiritbox");
	}

	@Override
	public void unequip() {
		source.setLooping(false);
	}

}
