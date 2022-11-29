package map.architecture.read;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.cmd.Console;
import gl.res.Texture;
import gl.res.TextureUtils;
import gr.zdimensions.jsquish.Squish;
import io.FileUtils;
import map.architecture.components.ArcTextureData;
import map.architecture.components.ArcTextureMapping;
import map.architecture.vis.Bsp;
import util.CounterInputStream;

public class ArcLoadTextures {

	static void readTextureInfo(Bsp bsp, ArcTextureData textureData, CounterInputStream in,
			boolean hasBakedLighting) throws IOException {
		
		String skybox = FileUtils.readString(in);
		textureData.setSkybox(skybox);

		ArcTextureMapping[] texMappings = new ArcTextureMapping[in.readInt()];
		for (int i = 0; i < texMappings.length; ++i) {
			texMappings[i] = new ArcTextureMapping();
			texMappings[i].textureId = in.readInt();
			texMappings[i].texels[0][0] = in.readFloat();
			texMappings[i].texels[0][1] = in.readFloat();
			texMappings[i].texels[0][2] = in.readFloat();
			texMappings[i].texels[0][3] = in.readFloat();
			texMappings[i].texels[1][0] = in.readFloat();
			texMappings[i].texels[1][1] = in.readFloat();
			texMappings[i].texels[1][2] = in.readFloat();
			texMappings[i].texels[1][3] = in.readFloat();
			
			if (hasBakedLighting) {
				texMappings[i].lmVecs[0][0] = in.readFloat();
				texMappings[i].lmVecs[0][1] = in.readFloat();
				texMappings[i].lmVecs[0][2] = in.readFloat();
				texMappings[i].lmVecs[0][3] = in.readFloat();
				texMappings[i].lmVecs[1][0] = in.readFloat();
				texMappings[i].lmVecs[1][1] = in.readFloat();
				texMappings[i].lmVecs[1][2] = in.readFloat();
				texMappings[i].lmVecs[1][3] = in.readFloat();
			}
		}

		bsp.setTextureMappings(texMappings);
	}

	private static final Squish.CompressionType compType = Squish.CompressionType.DXT1;
	
	static void readTextureList(ArcTextureData packedAssets, CounterInputStream in) throws IOException {
		String[] textures = new String[in.readInt()];
		int i = 0;
		List<Texture> textureDatas = new LinkedList<>();

		while(i < textures.length) {
			String textureName = FileUtils.readString(in);
			byte flags = in.readByte();
			//boolean translucent = (flags % 1) != 0;
			boolean noDataPresent = (flags & 2) != 0;
			boolean hasBumpMap = (flags & 4) != 0;
			boolean hasSpecMap = (flags & 8) != 0;
			
			if (!noDataPresent) {
				byte material = in.readByte();
				int width = in.readShort();
				int height = in.readShort();
				byte[] textureData = readBytes(in, in.readInt());
				String diffuseName = '$' + textureName;
				textures[i++] = diffuseName;
				byte[] diffuseData = Squish.decompressImage(null, width, height, textureData, compType);
				Texture t = TextureUtils.createTexture(diffuseData, material, width, height, true);
				textureDatas.add(t);

				if (hasBumpMap) {
					textureData = readBytes(in, in.readInt());
					byte[] bumpMapData = Squish.decompressImage(null, width, height, textureData, compType);
					String bumpTexName = '%' + textureName;
					textures[i++] = bumpTexName;
					t = TextureUtils.createTexture(bumpMapData, GL11.GL_RGB, material, width, height, true);
					textureDatas.add(t);
				}
				
				if (hasSpecMap) {
					textureData = readBytes(in, in.readInt());
					byte[] specMapData = Squish.decompressImage(null, width, height, textureData, compType);
					String specTexName = '&' + textureName;
					textures[i++] = specTexName;
					t = TextureUtils.createTexture(specMapData, GL30.GL_R8, material, width, height, true);
					textureDatas.add(t);
				}
			} else {
				textures[i++] = textureName;
				packedAssets.numToolTextures++;
				textureDatas.add(Resources.DEFAULT);
			}
			
			packedAssets.setTextureData(textureDatas, textures);
		}
	}

	private static byte[] readBytes(CounterInputStream in, int dataLen) throws IOException {
		byte[] data = new byte[dataLen];
		for (int l = 0; l < dataLen; l++) {
			data[l] = in.readByte();
		}
		return data;
	}
}
