package map.architecture.vis;

public class Pvs {
	
	private int numClusters;
	private int[][] ptrs;
	private byte[] vis;

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}
	
	public int getNumClusters() {
		return numClusters;
	}

	public void setClusterPointers(int[][] ptrs) {
		this.ptrs = ptrs;
	}

	public void setVisData(byte[] vis) {
		this.vis = vis;
	}
	
	public byte[] getVisData() {
		return vis;
	}
	
	private int getStartingIndex(int ptr, int dataType) {
		return ptrs[ptr][dataType];
	}
	
	public int[] getData(BspLeaf leaf, int pointer) {
		int start = getStartingIndex(leaf.clusterId, pointer);
		int[] clusterIndices = new int[numClusters];
		int i = start;
		for(int c = 0; c < numClusters; i++) {	
			
			if (vis[i] == 0) {
				i++;
				c += vis[i] * 8;	// Zero length compression
			} else {
				for(int bit = 1; bit != 256; bit *= 2) {
					if ((vis[i] & bit) != 0) {
						clusterIndices[c] = 1;
					}
					c++;
				}
			}
		}
		
		return clusterIndices;
	}
}
