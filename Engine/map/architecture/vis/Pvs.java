package map.architecture.vis;

public class Pvs {
	
	private int numClusters;
	private int[][] ptrs;
	private byte[] vis;
	
	public static final int PVS_DATA = 0, PAS_DATA = 1;

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
		return ptrs[ptr][dataType] - ptrs[0][dataType];
	}
	
	public int[] getClustersToRender(BspLeaf leaf) {
		int start = getStartingIndex(leaf.clusterId, 0);
		int[] clusterIndices = new int[numClusters];
		int i = start;

		for(int c = 0; c < numClusters; i++) {		
			if (vis[i] == 0) {
				i++;
				c += vis[i] * 8;
			} else {
				//System.out.println(String.format("%8s", Integer.toBinaryString(vis[i] & 0xFF)).replace(' ', '0'));
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
	
	//The decoding of the run-length compression works as follows: To find the PVS of a given cluster, start at the byte given by the
	//offset in the ptrs[] array. If the current byte in the PVS buffer is zero, the following byte multiplied by 8 is the number
	//of clusters to skip that are not visible. If the current byte is non-zero, the bits that are set correspond to clusters that are
	//visible from this cluster. Continue until the number of clusters in the map is reached. 
}
