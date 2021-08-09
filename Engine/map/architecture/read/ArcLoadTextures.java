package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import gr.zdimensions.jsquish.Squish;
import io.FileUtils;
import map.architecture.Architecture;
import map.architecture.components.ArcPackedAssets;
import map.architecture.components.ArcPackedTexture;
import map.architecture.components.ArcTextureData;

public class ArcLoadTextures {

	static void readTextureInfo(ArcPackedAssets packedAssets, DataInputStream in,
			boolean hasBakedLighting) throws IOException {
		
		String skybox = FileUtils.readString(in);
		packedAssets.skybox = skybox;

		ArcTextureData[] texData = new ArcTextureData[in.readInt()];
		for (int i = 0; i < texData.length; ++i) {
			texData[i] = new ArcTextureData();
			texData[i].textureId = in.readInt();
			texData[i].texels[0][0] = in.readFloat();
			texData[i].texels[0][1] = in.readFloat();
			texData[i].texels[0][2] = in.readFloat();
			texData[i].texels[0][3] = in.readFloat();
			texData[i].texels[1][0] = in.readFloat();
			texData[i].texels[1][1] = in.readFloat();
			texData[i].texels[1][2] = in.readFloat();
			texData[i].texels[1][3] = in.readFloat();
			if (hasBakedLighting) {
				texData[i].lmVecs[0][0] = in.readFloat();
				texData[i].lmVecs[0][1] = in.readFloat();
				texData[i].lmVecs[0][2] = in.readFloat();
				texData[i].lmVecs[0][3] = in.readFloat();
				texData[i].lmVecs[1][0] = in.readFloat();
				texData[i].lmVecs[1][1] = in.readFloat();
				texData[i].lmVecs[1][2] = in.readFloat();
				texData[i].lmVecs[1][3] = in.readFloat();
			}
		}

		packedAssets.setTextureData(texData);
	}

	static void readTextureList(ArcPackedAssets packedAssets, DataInputStream in) throws IOException {
		String[] textures = new String[in.readInt()];
		for (int i = 0; i < textures.length; ++i) {
			textures[i] = '%' + FileUtils.readString(in);
			/*
			 * STRING name BYTE compression BYTE material SHORT width SHORT height INT
			 * dataLen BYTE[] data
			 */
			byte compression = in.readByte();
			if (compression != 0) {
				byte material = in.readByte();
				int width = in.readShort();
				int height = in.readShort();
				int dataLen = in.readInt();
				byte[] textureData = new byte[dataLen];
				for (int l = 0; l < dataLen; l++) {
					textureData[l] = in.readByte();
				}
				Squish.CompressionType compType;
				switch (compression) {
				case 1:
					compType = Squish.CompressionType.DXT1;
					break;
				case 3:
					compType = Squish.CompressionType.DXT3;
					break;
				default:
					compType = Squish.CompressionType.DXT5;
				}
				byte[] decompressedData = Squish.decompressImage(null, width, height, textureData, compType);
				ArcPackedTexture t = new ArcPackedTexture(textures[i], material, decompressedData, width, height);
				packedAssets.add(textures[i], t);
			} else {
				packedAssets.numToolTextures++;
			}
		}
	}
}
