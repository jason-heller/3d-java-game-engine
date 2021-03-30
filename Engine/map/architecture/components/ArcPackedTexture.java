package map.architecture.components;

import core.Resources;
import gl.res.Texture;

public class ArcPackedTexture {

	private String id;
	private byte material;
	private byte[] decompressedData;
	private int width, height;
	
	public ArcPackedTexture(String id, byte material, byte[] decompressedData, int width, int height) {
		this.id = id;
		this.material = material;
		this.decompressedData = decompressedData;
		this.width = width;
		this.height = height;
	}

	public Texture passToOpenGL() {
		return Resources.addTexture(id, material, decompressedData, width, height, true);
	}
}
