package io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.joml.Vector3f;

import dev.cmd.Console;
import map.Rail;
import map.RailList;

public class RailFileLoader {

	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports
	
	public static RailList[] readRailFile(String mapFileName) {
		String path = "maps/" + mapFileName + ".rail";
		
		File f = new File(path);
		if (!f.exists()) {
			Console.severe("Attempted to load rail file, but file does not exist (" + f + ")");
			return null;
		}
		
		RailList[] railList = null;
		
		try(DataInputStream is = new DataInputStream(new FileInputStream(f))) {
			// Header
			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();

			if (version != EXPECTED_VERSION) {
				Console.severe("Rail file is incorrect version, expected version " + EXPECTED_VERSION + ", got " + version);
				return null;
			}

			if (!fileExtName.equals("RAIL")) {
				Console.severe("Attempted to load rail file, but read something else.");
				return null;
			}
			
			int numRails = is.readShort();
			int numBlocksX = is.readShort();
			int numBlocksZ = is.readShort();
			
			RailList.numBlocksX = numBlocksX;
			RailList.numBlocksZ = numBlocksZ;
			
			Rail[] rails = new Rail[numRails];
			railList = new RailList[numBlocksX * numBlocksZ];

			for(int i = 0; i < numRails; i++) {
				Vector3f start = readVec3(is);
				Vector3f end = readVec3(is);
				byte type = is.readByte();
				
				rails[i] = new Rail(start, end, "");
				
				while(true) {
					int data = is.readShort();
					int blockId = Math.abs(data) - 1;
					
					RailList block = railList[blockId];
					
					if (block == null) {
						block = new RailList();
						railList[blockId] = block;
					}
					
					block.addRail(rails[i]);
					
					if (data < 0)
						break;
				}
			}
			
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return railList;
	}
	
	private static Vector3f readVec3(DataInputStream is) throws IOException {
		return new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
	}
}
