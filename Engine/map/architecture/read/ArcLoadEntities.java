package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import io.FileUtils;
import map.architecture.Architecture;
import map.architecture.functions.commands.CamView;
import map.architecture.functions.commands.PathNode;
import map.architecture.functions.commands.SpawnPoint;
import scene.entity.EntityHandler;
import scene.entity.object.RopePointEntity;
import scene.entity.object.SolidPhysProp;

public class ArcLoadEntities {
	static void readEntities(Architecture arc, DataInputStream in) throws IOException {
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
			case "path_node":
				PathNode pathNode = new PathNode(readVec3(tags, "pos"), readInt(tags, "id"), readInt(tags, "next"), readInt(tags, "prev"), tags.get("cmd"));
				arc.addFunction(pathNode);
				break;
			case "cam_view":
				CamView camView = new CamView(readVec3(tags, "pos"), readVec3(tags, "rot"));
				arc.addFunction(camView);
				break;
			case "prop":
				SolidPhysProp prop = new SolidPhysProp(readVec3(tags, "pos"), readVec3(tags, "rot"), tags.get("model"));
				EntityHandler.addEntity(prop);
				break;
			case "rope_node":
				RopePointEntity rpe = new RopePointEntity(readVec3(tags, "pos"), tags.get("name"), tags.get("next"),
						readFloat(tags, "give"), readInt(tags, "precision"), readVec3(tags, "color"), readFloat(tags, "speed"));
				EntityHandler.addEntity(rpe);
				break;
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
