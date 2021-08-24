package gl.res;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import gl.Render;
import map.architecture.Material;

public class Texture {
	public final int id;
	public int width, height;

	private final int type;

	private int atlasRows = 1;

	private boolean transparent = false;
	private Material material = Material.ROCK;

	public Texture(int id, int width, int height, boolean transparent, int atlasRows) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.type = GL11.GL_TEXTURE_2D;
		this.transparent = transparent;
		this.atlasRows = atlasRows;
	}

	public Texture(int id, int type, int width, int height, boolean transparent, int atlasRows) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.type = type;
		this.transparent = transparent;
		this.atlasRows = atlasRows;
	}

	public void bind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(type, id);
		Render.textureSwaps++;
	}
	
	public void unbind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(type, 0);
		Render.textureSwaps++;
	}

	public static void unbindTexture(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		Render.textureSwaps++;
	}
	
	public void delete() {
		GL11.glDeleteTextures(id);
	}

	public int getTextureAtlasRows() {
		return atlasRows;
	}

	public boolean isTransparent() {
		return transparent;
	}
	
	public void setMaterial(byte mat) {
		material = Material.values()[mat];
	}
	
	public Material getMaterial() {
		return material;
	}
}
