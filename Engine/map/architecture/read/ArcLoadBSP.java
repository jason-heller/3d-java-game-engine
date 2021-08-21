package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import org.joml.Vector3f;

import map.architecture.components.ArcTextureData;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.BspNode;

public class ArcLoadBSP {
	
	static void readBspTree(Bsp bsp, DataInputStream in) throws IOException {
		short[] leafIds = new short[in.readInt()];
		for (int i = 0; i < leafIds.length; i++) {
			leafIds[i] = in.readShort();
		}
		
		bsp.leafFaceIndices = leafIds;

		BspNode[] nodes = new BspNode[in.readInt()];
		for (int i = 0; i < nodes.length; ++i) {
			nodes[i] = new BspNode();
			nodes[i].planeNum = in.readInt();
			nodes[i].childrenId[0] = in.readInt();
			nodes[i].childrenId[1] = in.readInt();
			nodes[i].min = new Vector3f(in.readShort(), in.readShort(), in.readShort());
			nodes[i].max = new Vector3f(in.readShort(), in.readShort(), in.readShort());
			nodes[i].firstFace = in.readShort();
			nodes[i].numFaces = in.readShort();
		}

		bsp.setNodes(nodes);

		// leafs
		BspLeaf[] leaves = new BspLeaf[in.readInt()];
		int numClusterLeaves = 0;
		for (int i = 0; i < leaves.length; ++i) {
			BspLeaf leaf = new BspLeaf();
			leaf.clusterId = in.readShort();
			leaf.min = new Vector3f(in.readShort(), in.readShort(), in.readShort());
			leaf.max = new Vector3f(in.readShort(), in.readShort(), in.readShort());
			leaf.firstFace = in.readShort();
			leaf.numFaces = in.readShort();
			leaf.firstAmbientSample = in.readShort();
			leaf.numAmbientSamples = in.readShort();
			leaf.isUnderwater = in.readByte() != -1;
			leaf.room = in.readByte();
			
			int numClipsInLeaf = in.readByte();
			leaf.clips = new short[numClipsInLeaf];
			for(int j = 0; j < numClipsInLeaf; j++) {
				leaf.clips[j] = in.readShort();
			}
			
			int numHeightmapsInLeaf = in.readByte();
			leaf.heightmaps = new short[numHeightmapsInLeaf];
			for(int j = 0; j < numHeightmapsInLeaf; j++) {
				leaf.heightmaps[j] = in.readShort();
			}

			if (leaf.clusterId != -1) {
				numClusterLeaves++;
			}
			
			leaves[i] = leaf;
		}
		bsp.leaves = leaves;

		BspLeaf[] clusters = new BspLeaf[numClusterLeaves];
		int j = 0;
		for (int i = 0; i < leaves.length; ++i) {
			if (leaves[i].clusterId != -1) {
				clusters[j++] = leaves[i];
			}
		}
		bsp.clusters = clusters;

		// face ids for leafs
		short[] faceIds = new short[in.readInt()];

		for (int i = 0; i < faceIds.length; ++i) {
			faceIds[i] = in.readShort();
		}
	}
}
