package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import map.architecture.Architecture;
import map.architecture.components.ArcLightCube;
import map.architecture.vis.Bsp;

public class ArcLoadLighting {

	public static void readLighting(Architecture arc, DataInputStream in, boolean hasBakedLighting) throws IOException {
		Bsp bsp = arc.bsp;
		
		byte[] rgb = null;
		ArcLightCube[] lightCubes = null;
		if (hasBakedLighting) {
			int lmDataLenBytes = in.readInt();
			rgb = new byte[lmDataLenBytes];
			for(int i = 0; i < lmDataLenBytes; i++) {
				rgb[i] = in.readByte();
			}
			
			// Ambient lighting
			int numLightCubes = in.readInt();
			final int[] order = new int[] {0, 1, 4, 5, 2, 3};	// Oops
			lightCubes = new ArcLightCube[numLightCubes];
			for(int i = 0; i < numLightCubes; i++) {
				ArcLightCube lightCube = new ArcLightCube();
				//byte[] r = new byte[6], g = new byte[6], b = new byte[6], a = new byte[6];
				int[] colors = new int[6];
				for(int k = 0; k < 6; k++) {
					//lightInfoCompressed[k] = in.readInt() & 0xFFFFFFFF;	// r,g,b,exp
					int ch1 = in.read();
			        int ch2 = in.read();
			        int ch3 = in.read();
			        int ch4 = in.readByte();
					colors[order[k]] = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
				}
				
				lightCube.colors = colors;
				
				lightCube.x = in.read() / 255f;
				lightCube.y = in.read() / 255f;
				lightCube.z = in.read() / 255f;
				
				lightCubes[i] = lightCube;
			}
		}
		
		if (hasBakedLighting) {
			arc.createLightmap(rgb, bsp.faces);
			arc.ambientLightCubes = lightCubes;
		}
	}

}
