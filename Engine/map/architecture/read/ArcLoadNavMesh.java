package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import org.joml.Vector3f;

import io.FileUtils;
import map.architecture.components.ArcNavigation;

public class ArcLoadNavMesh {
	
	static void readNavMesh(ArcNavigation nav, DataInputStream in) throws IOException {
		short numNavElements = 0;
		
		numNavElements = in.readShort();
		nav.initNarrowphase(numNavElements);
		for(int i = 0; i < numNavElements; i++) {
			Vector3f position = FileUtils.readVec3(in);
			int numFaces = in.readByte();
			int[] navFaces = new int[numFaces];
			for(int n = 0; n < numFaces; n++) {
				navFaces[n] = in.readInt();
			}
			short[] neighbors = new short[in.readByte()];
			int[] neighborEdges = new int[neighbors.length];
			
			for(int n = 0; n < neighbors.length; n++) {
				neighbors[n] = in.readShort();
				neighborEdges[n] = in.readInt();
			}
			nav.addNode(i, position, navFaces, neighbors, neighborEdges);
		}
	}
}
