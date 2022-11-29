package map.architecture.read;

import java.io.IOException;

import org.joml.Vector3f;

import io.FileUtils;
import map.architecture.components.ArcNavigation;
import util.CounterInputStream;

public class ArcLoadNavMesh {
	
	static void readNavMesh(ArcNavigation nav, CounterInputStream in) throws IOException {
		short numNavElements = 0;
		
		numNavElements = in.readShort();
		nav.initNarrowphase(numNavElements);
		for(int i = 0; i < numNavElements; i++) {
			Vector3f position = FileUtils.readVec3(in);
			int planeId = in.readInt();
			float width = in.readFloat();
			float length = in.readFloat();
			short[] neighbors = new short[in.readByte()];
			//int[] sharedEdgeIds = new int[neighbors.length];
			
			for(int n = 0; n < neighbors.length; n++) {
				neighbors[n] = in.readShort();
				//sharedEdgeIds[n] = in.readShort();
			}
			nav.addNode(i, position, planeId, neighbors, width, length);
		}
	}
}
