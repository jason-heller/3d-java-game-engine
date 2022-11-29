package map.architecture.read;

import java.io.IOException;

import map.architecture.vis.Pvs;
import util.CounterInputStream;

public class ArcLoadVis {
	static void readVis(Pvs pvs, CounterInputStream in) throws IOException {
		int numClusters = in.readInt();
		pvs.setNumClusters(numClusters);

		int[][] clusterPointers = new int[numClusters][2];
		for (int i = 0; i < numClusters; ++i) {
			clusterPointers[i][0] = in.readInt();
			clusterPointers[i][1] = in.readInt();
		}

		pvs.setClusterPointers(clusterPointers);

		int visLen = in.readInt();
		byte[] visData = new byte[visLen];
		for (int i = 0; i < visLen; ++i) {
			visData[i] = in.readByte();
		}

		pvs.setVisData(visData);
	}
}
