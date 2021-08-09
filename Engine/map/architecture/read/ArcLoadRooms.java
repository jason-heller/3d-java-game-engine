package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import io.FileUtils;
import map.architecture.components.ArcRoom;
import map.architecture.components.GhostPoi;
import map.architecture.vis.Bsp;

public class ArcLoadRooms {
	
	static void readRooms(Bsp bsp, DataInputStream in) throws IOException {
		final int numRooms = in.readByte() + 1;
		bsp.rooms = new ArcRoom[numRooms];
		bsp.rooms[0] = ArcRoom.DEFAULT_ROOM;
		for(int i = 1; i < numRooms; i++) {
			int type = in.readByte();
			String name = FileUtils.readString(in);
			int numPois = in.readByte();
			GhostPoi[] pois = new GhostPoi[numPois];
			for(int j = 0; j < numPois; j++) {
				pois[j] = new GhostPoi(FileUtils.readVec3(in), FileUtils.readVec3(in), FileUtils.readString(in));
			}
			bsp.rooms[i] = new ArcRoom(name, type, pois);
		}
	}
}
