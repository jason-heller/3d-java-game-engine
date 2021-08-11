package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import org.joml.Vector3f;

import geom.AxisAlignedBBox;
import io.FileUtils;
import map.architecture.components.ArcHeightmap;
import map.architecture.components.ArcHeightmapVertex;
import map.architecture.vis.Bsp;

public class ArcLoadHeightmaps {
	static void readHeightmaps(Bsp bsp, DataInputStream in) throws IOException {
		
		final int numHeightVerts = in.readInt();
		ArcHeightmapVertex[] heightmapVerts = new ArcHeightmapVertex[numHeightVerts];
		
		for(int i = 0; i < numHeightVerts; i++) {
			heightmapVerts[i] = new ArcHeightmapVertex(in.readFloat(), in.readFloat());
		}
		
		final int numHeightMaps = in.readInt();
		ArcHeightmap[] heightmaps = new ArcHeightmap[numHeightMaps];
		for(int i = 0; i < numHeightMaps; i++) {
			Vector3f origin = FileUtils.readVec3(in);
			AxisAlignedBBox bbox = new AxisAlignedBBox(FileUtils.readVec3(in), FileUtils.readVec3(in));
			heightmaps[i] = new ArcHeightmap(origin, bbox, in.readInt(), in.readInt(), in.readInt(), in.readInt(),
					in.readInt(), in.readShort(), in.readShort());
		}

		bsp.heightmapVerts = heightmapVerts;
		bsp.heightmaps = heightmaps;
	}
}
