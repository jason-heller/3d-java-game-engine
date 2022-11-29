package io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import core.Resources;
import gl.res.Texture;
import gl.res.TextureUtils;
import gr.zdimensions.jsquish.Squish;

public class EnvironmentMapFileLoader {

	private static final Squish.CompressionType compType = Squish.CompressionType.DXT1;
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports
	
	public static Map<Integer, Texture> readEMP(String mapFileName) {
		String path = "maps/" + mapFileName + ".emp";

		Map<Integer, Texture> textures = new HashMap<>();
		
		File f = new File(path);
		if (!f.exists())
			return null;
		
		try(DataInputStream is = new DataInputStream(new FileInputStream(f))) {
			// Header
			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();

			if (version != EXPECTED_VERSION)
				return null;

			if (!fileExtName.equals("EMP"))
				return null;
			
			int width = is.readShort();
			int height = is.readShort();

			byte[][] rawTextureData = new byte[6][];
			
			int clipIndex;
			while((clipIndex = is.readShort()) != -1) {
				
				for(int currentTexture = 0; currentTexture < 6; currentTexture++) {
					int dataLen = is.readInt();
					byte[] data = new byte[dataLen];
					//while(is.read(data, 0, dataLen)  != -1) {}
					for(int i  = 0; i < dataLen; i++) {
						data[i] = is.readByte();
					}
					
					rawTextureData[currentTexture] = Squish.decompressImage(null, width, height, data, compType);
				}
			
				Texture texture = TextureUtils.createTexture(rawTextureData, width, height, true);
				textures.put(clipIndex, texture);
			}
			
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return textures;
	}
}
