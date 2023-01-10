package map.architecture.read;

import java.io.IOException;

import org.joml.Vector3f;

import geom.Plane;
import io.FileUtils;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.vis.Bsp;
import util.CounterInputStream;

public class ArcLoadGeometry {
	static void readGeometry(Bsp bsp, Vector3f offset, CounterInputStream in) throws IOException {
		// Planes
		Plane[] planes = new Plane[in.readInt()];
		for (int i = 0; i < planes.length; ++i) {
			planes[i] = new Plane();
			planes[i].normal = FileUtils.readVec3(in);
			planes[i].dist = in.readFloat();
		}
		bsp.planes = planes;

		// Verts
		Vector3f[] verts = new Vector3f[in.readInt()];
		for (int i = 0; i < verts.length; ++i) {
			verts[i] = FileUtils.readVec3(in);
			verts[i].add(offset);
		}
		bsp.vertices = verts;
		bsp.min = FileUtils.readVec3(in);
		bsp.max = FileUtils.readVec3(in);

		// Edges
		ArcEdge[] edges = new ArcEdge[in.readInt()];
		for (int i = 0; i < edges.length; ++i) {
			edges[i] = new ArcEdge();
			edges[i].start = in.readInt();
			edges[i].end = in.readInt();
		}
		bsp.edges = edges;

		// Surfedges
		int[] surfEdges = new int[in.readInt()];
		for (int i = 0; i < surfEdges.length; ++i) {
			surfEdges[i] = in.readInt();
		}
		bsp.surfEdges = surfEdges;

		// faces
		ArcFace[] faces = new ArcFace[in.readInt()];
		for (int i = 0; i < faces.length; ++i) {
			faces[i] = new ArcFace();
			/*faces[i].onNode = */in.readByte();
			faces[i].planeId = in.readShort();
			faces[i].firstEdge = in.readInt();
			faces[i].numEdges = in.readShort();
			faces[i].texMapping = in.readShort();
			faces[i].lmIndex = in.readInt();
			faces[i].lmMins = new float[] {in.readFloat(), in.readFloat()};
			faces[i].lmSizes = new float[] {in.readFloat(), in.readFloat()};
			faces[i].lmStyles = new byte[] {in.readByte(), in.readByte(), in.readByte(), in.readByte()};
			// NOTE: lmStyles has a stride of (lmSizes[0] + 1) * (lmSizes[1] + 1)
			// totalLuxels = (number of lmStyles) * (lmSizes[0] + 1) * (lmSizes[1] + 1)

		}
		bsp.faces = faces;
	}
}
