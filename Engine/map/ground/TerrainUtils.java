package map.ground;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TerrainUtils {

	public static int[][] readHeightFile(String path) {
		try {
			return readHeightFile(new DataInputStream(new FileInputStream("src/res/" + path)));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int[][] readHeightFile(DataInputStream dis) throws IOException {
		final short size = dis.readShort();
		final int xSize = size / 4;
		int[][] data = new int[xSize][size];
		
		for(int x = 0; x < xSize; x++) {
			for(int z = 0; z < size; z++) {
				int val = dis.readByte();
				val = val << 8 | dis.readByte();
				val = val << 8 | dis.readByte();
				val = val << 8 | dis.readByte();
				
				data[x][z] = val;
			}
		}
		
		return data;
	}

	
}
