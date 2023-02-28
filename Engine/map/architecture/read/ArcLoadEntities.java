package map.architecture.read;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import dev.cmd.Console;
import gl.skybox._3d.SkyboxCamera;
import io.FileUtils;
import map.architecture.Architecture;
import map.architecture.functions.commands.CamView;
import map.architecture.functions.commands.PathNode;
import map.architecture.functions.commands.SoundScape;
import map.architecture.functions.commands.SpawnPoint;
import scene.entity.EntityHandler;
import scene.entity.goal.GapGoal;
import scene.entity.goal.GrindDistGoal;
import scene.entity.goal.ScoreGoal;
import scene.entity.goal.TrickStringGoal;
import scene.entity.object.SolidPhysProp;
import scene.entity.object.map.AmbientSource;
import scene.entity.object.map.DecalEntity;
import scene.entity.object.map.LightPointEntity;
import scene.entity.object.map.LightSpotEntity;
import scene.entity.object.map.RopePointEntity;
import scene.entity.util.GapEntity;
import scene.entity.util.GapTrigger;
import scene.entity.util.LightStyle;
import util.CounterInputStream;

public class ArcLoadEntities {
	static void readEntities(Architecture arc, CounterInputStream in) throws IOException {
		int numEnts = in.readInt();
		
		for (int i = 0; i < numEnts; i++) {
			String name = FileUtils.readString(in);

			byte numTags = in.readByte();
			Map<String, String> tags = new HashMap<String, String>();
			for (byte k = 0; k < numTags; k++) {
				String key = FileUtils.readString(in);
				String val = FileUtils.readString(in);
				tags.put(key, val);
			}
			
			switch(name) {

			case "spawn_player":
				SpawnPoint spawn = new SpawnPoint(readVec3(tags, "pos"), readVec3(tags, "rot"), tags.get("label"));
				arc.addFunction(spawn);
				break;
			case "sky_view":
				SkyboxCamera skyCamera = new SkyboxCamera(readVec3(tags, "pos"),
						readFloat(tags, "scale"), readInt(tags, "has_fog") != 0, readFloat(tags, "fog_start"),
						readFloat(tags, "fog_end"), readVec3(tags, "fog_color"));
				arc.setSkyCamera(skyCamera);
				break;
			case "path_node":
				PathNode pathNode = new PathNode(readVec3(tags, "pos"), readInt(tags, "id"), readInt(tags, "next"), readInt(tags, "prev"), tags.get("cmd"));
				arc.addFunction(pathNode);
				break;
			case "cam_view":
				CamView camView = new CamView(readVec3(tags, "pos"), readVec3(tags, "rot"));
				arc.addFunction(camView);
				break;
			case "ambient_sfx":
				SoundScape soundScape = new SoundScape(readVec3(tags, "pos"), tags.get("sfx"));
				arc.addFunction(soundScape);
				break;
			case "prop":
				SolidPhysProp prop = new SolidPhysProp(readVec3(tags, "pos"), readVec3(tags, "rot"), tags.get("model"));
				EntityHandler.addEntity(prop);
				break;
			case "rope_node":
				RopePointEntity rpe = new RopePointEntity(readVec3(tags, "pos"), tags.get("name"), tags.get("next"),
						readFloat(tags, "give"), readInt(tags, "precision"), readVec3(tags, "color"),
						readFloat(tags, "speed"));
				EntityHandler.addEntity(rpe);
				break;
			case "light_point":
				
				LightPointEntity lpe = new LightPointEntity(readVec3(tags, "pos"),
						LightStyle.getStyleFromId(readInt(tags, "style")), readFloat(tags, "attn_linear"),
						readFloat(tags, "attn_quadratic"), readVec3(tags, "color"));
				EntityHandler.addEntity(lpe);
				break;
			case "light_spot":
				LightSpotEntity lse = new LightSpotEntity(readVec3(tags, "pos"), readVec3(tags, "rot"),
						LightStyle.getStyleFromId(readInt(tags, "style")), readFloat(tags, "attn_linear"),
						readFloat(tags, "attn_quadratic"), readVec3(tags, "color"));
				EntityHandler.addEntity(lse);
				break;
			case "sfx_source":
				AmbientSource ambientSource = new AmbientSource(tags.get("name"), readVec3(tags, "pos"),
						tags.get("sfx"), readFloat(tags, "vol"), readFloat(tags, "dist"));
				EntityHandler.addEntity(ambientSource);
				break;
			case "decal":
				DecalEntity decal = new DecalEntity(readVec3(tags, "pos"), tags.get("tex"));
				EntityHandler.addEntity(decal);
				break;
				
			case "goal_score":
				EntityHandler.addEntity(new ScoreGoal(readInt(tags, "difficulty"), readInt(tags, "score"), readInt(tags, "is_combo") == 1));
				break;
				
			case "goal_grind_dist":
				EntityHandler.addEntity(new GrindDistGoal(readInt(tags, "difficulty"), readFloat(tags, "dist")));
				break;
				
			case "goal_trick_string":
				EntityHandler.addEntity(new TrickStringGoal(readInt(tags, "difficulty"), tags.get("trick_str")));
				break;
				
			case "goal_gap":
				EntityHandler.addEntity(new GapGoal(readInt(tags, "difficulty"), tags.get("trick_str"), tags.get("gaps"), tags.get("text")));
				break;

			case "gap_node":
				EntityHandler.addEntity(new GapTrigger(readVec3(tags, "min"), readVec3(tags, "max"),
						readInt(tags, "node"), readInt(tags, "first_face"), readInt(tags, "num_faces"),
						tags.get("name"), readInt(tags, "flags")));
				break;
				
			case "gap":
				EntityHandler.addEntity(new GapEntity(tags.get("name"), readInt(tags, "points"), tags.get("t1"), tags.get("t2")));
				break;

			default:
				Console.warning("Unknown entity loaded: " + name);
			}
		}
	}

	private static float readFloat(Map<String, String> tags, String string) {
		String data = tags.get(string);
		return data.equals("") ? 0f : Float.parseFloat(data);
	}

	private static int readInt(Map<String, String> tags, String string) {
		String data = tags.get(string);

		return data.equals("") ? 0 : Integer.parseInt(data);
	}

	private static Vector3f readVec3(Map<String, String> tags, String string) {
		String[] data = tags.get(string).split(",");
		return new Vector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
	}
}
