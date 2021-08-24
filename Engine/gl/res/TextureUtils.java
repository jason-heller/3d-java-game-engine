package gl.res;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import dev.cmd.Console;
import gl.Render;
import io.FileUtils;

class TextureData {

	public int type;
	private final int width;
	private final int height;
	private final ByteBuffer buffer;

	private boolean clampEdges = false;
	private boolean mipmap = false;
	private boolean anisotropic = false;
	private boolean nearest = true;
	private boolean transparent;
	private float bias = Render.defaultBias;
	private int numRows = 1;
	private int format = GL11.GL_RGBA;

	public TextureData(ByteBuffer buffer, int width, int height) {
		this.buffer = buffer;
		this.width = width;
		this.height = height;
	}
	
	public int getFormat() {
		return format;
	}

	public float getBias() {
		return bias;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public int getHeight() {
		return height;
	}

	public int getNumRows() {
		return numRows;
	}

	public int getWidth() {
		return width;
	}

	public boolean isAnisotropic() {
		return anisotropic;
	}

	public boolean isClampEdges() {
		return clampEdges;
	}

	public boolean isMipmap() {
		return mipmap;
	}

	public boolean isNearest() {
		return nearest;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void setAnisotropic(boolean anisotropic) {
		this.anisotropic = anisotropic;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}

	public void setClampEdges(boolean clampEdges) {
		this.clampEdges = clampEdges;
	}

	public void setMipmap(boolean mipmap) {
		this.mipmap = mipmap;
	}

	public void setNearest(boolean nearest) {
		this.nearest = nearest;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}
}

public class TextureUtils {

	
	public static Texture createTexture(byte[] rgba, byte material, int width, int height, boolean mipmap) {
		return createTexture(rgba, GL11.GL_RGBA, material, width, height, mipmap);
	}
	
	public static Texture createTexture(byte[] colorData, int format, byte material, int width, int height, boolean mipmap) {
		final ByteBuffer buf = BufferUtils.createByteBuffer(colorData.length);
		buf.put(colorData);
		buf.flip();

		final TextureData textureData = new TextureData(buf, width, height);
		textureData.type = GL11.GL_TEXTURE_2D;
		textureData.setMipmap(mipmap);
		final int textureId = loadTextureToOpenGL(textureData);
		
		Texture tex = new Texture(textureId, textureData.getWidth(), textureData.getHeight(), true, 0);
		tex.setMaterial(material);
		return tex;
	}

	public static Texture createTexture(byte[][] rgba, int width, int height, boolean hasAlpha) {
		final int texID = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);

		final ByteBuffer buf = ByteBuffer.allocateDirect(width * height * (hasAlpha ? 4 : 3));
		int format = hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB;
		for (int i = 0; i < 6; i++) {
			buf.put(rgba[i]);

			buf.flip();
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, format, width, height, 0, GL11.GL_RGB,
					GL11.GL_UNSIGNED_BYTE, buf);
			buf.clear();
		}
		
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);

		return new Texture(texID, GL13.GL_TEXTURE_CUBE_MAP, width * 6, height, false, 0);
	}

	public static Texture createTexture(String path) {
		final TextureData textureData = readTextureData(path);
		if (textureData == null) {
			return null;
		}
		textureData.type = GL11.GL_TEXTURE_2D;
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, textureData.getWidth(), textureData.getHeight(), true, 0);
	}

	public static Texture createTexture(String path, int type, boolean nearest, boolean mipmap, float bias,
			boolean clampEdges, boolean isTransparent, int numRows) {
		final TextureData textureData = readTextureData(path);
		textureData.type = type;
		textureData.setNearest(nearest);
		textureData.setMipmap(mipmap);
		textureData.setBias(bias);
		textureData.setClampEdges(clampEdges);
		textureData.setTransparent(isTransparent);
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, type, textureData.getWidth(), isTransparent, numRows);
	}

	public static Texture createTexture(String path, int type, boolean isTransparent, int numRows) {
		final TextureData textureData = readTextureData(path);
		textureData.type = type;
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, type, textureData.getWidth(), isTransparent, numRows);
	}
	
	public static float[][][] getRawTextureData(String path) {
		TextureData td = readTextureData(path);
		
		int w = td.getWidth();
		int h = td.getHeight();
		
		// Haha super unoptimized
		ByteBuffer buf = td.getBuffer();
		float[][][] data = new float[w][h][3];
		byte[] byteArr = new byte[buf.remaining()];
		buf.get(byteArr);
		int byteArrInd = 0;
		
		for(int j = 0; j < h; j++) {
			for(int i = 0; i < w; i++) {
				// AGRB
				data[i][j][0] = (byteArr[byteArrInd+2] & 0xFF) / 255f;
				data[i][j][1] = (byteArr[byteArrInd+1] & 0xFF) / 255f;
				data[i][j][2] = (byteArr[byteArrInd+0] & 0xFF) / 255f;
				
				byteArrInd += 4;
			}
		}
		
		return data;
	}
	
	public static byte[][] getRawTextureData(String... paths) {
		byte[][] data = new byte[paths.length][];
		byte[] byteArr;
		
		for(int n = 0; n < paths.length; n++) {
			TextureData td = readTextureData("res/" + paths[n]);
			
			int w = td.getWidth();
			
			ByteBuffer buf = td.getBuffer();
			final int len = w * w * 3;
			data[n] = new byte[len];
			byteArr = new byte[buf.remaining()];
			buf.get(byteArr);
			int byteArrInd = 0;
			
			for(int i = 0; i < len; i+=3) {
				// AGRB
				data[n][i] = byteArr[byteArrInd+2];
				data[n][i+1] = byteArr[byteArrInd+1];
				data[n][i+2] = byteArr[byteArrInd+0];
				
				byteArrInd += 4;
			}
		}
		
		return data;
	}
	
	// +x, -x, +y, -y, +z, -z
	private static final int[] FACE_ORDER = new int[] {1, 4, 0, 5, 2, 3};
	public static byte[][] getRawCubemapTexData(String path) {
		byte[][] data = new byte[6][];
		byte[] byteArr;
		
		TextureData td = readTextureData("res/" + path);
		
		ByteBuffer buf = td.getBuffer();
		final int size = td.getHeight();
		final int len = size * size * 3;
		
		byteArr = new byte[buf.remaining()];
		buf.get(byteArr);
		int pixelIndex = 0;
		int newLinePxStripe = td.getWidth();
		int i = 0;
		
		try {
		for(i = 0; i < 6; i++) {
			final int faceIndex = FACE_ORDER[i];
			data[faceIndex] = new byte[len];
			pixelIndex = 0;
			
			for(int j = 0; j < len; j += 3) {
				// AGRB
				int index = (pixelIndex % size) + (newLinePxStripe * (pixelIndex / size));	// Index into pixel of partition
				index += size * i;	// Index into current partition
				index *= 4;					// Scale to start of ARGB data
				
				data[faceIndex][j]   = byteArr[index + 2];
				data[faceIndex][j+1] = byteArr[index + 1];
				data[faceIndex][j+2] = byteArr[index + 0];
				
				pixelIndex++;
			}
		}
		} catch(Exception e) {
			Console.log(byteArr.length +", " + i + ", " + pixelIndex);
			int index = (pixelIndex % size) + (newLinePxStripe * (pixelIndex / size));	// Index into pixel of partition
			Console.log("pixel #" + index);
			index += size * size * i;	// Index into current partition
			Console.log("pixel pos" + index);
			index *= 4;					// Scale to start of ARGB data
			Console.log("data pos" + index);
			
			e.printStackTrace();
		}
		
		return data;
	}
	
	protected static TextureData readTextureData(String path) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			final InputStream in = FileUtils.getInputStream(path);
			final PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.BGRA);
			buffer.flip();
			in.close();
		} catch (final Exception e) {
			e.printStackTrace();
			Console.log("Tried to load texture " + path + " , didn't work");
			return null;
		}
		return new TextureData(buffer, width, height);
	}

	private static int loadTextureToOpenGL(TextureData data) {
		final int texID = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(data.type, texID);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		if (data.type == GL13.GL_TEXTURE_CUBE_MAP) {
			for (int i = 0; i < 6; i++) {
				GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, data.getWidth(),
						data.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			}
		} else {
			GL11.glTexImage2D(data.type, 0, data.getFormat(), data.getWidth(), data.getHeight(), 0, GL12.GL_BGRA,
					GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}

		if (data.isMipmap()) {
			if (data.getNumRows() <= 1) {
				GL30.glGenerateMipmap(data.type);
			}
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);// GL11.
			GL11.glTexParameterf(data.type, GL14.GL_TEXTURE_LOD_BIAS, data.getBias());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 5);
			if (data.isAnisotropic() && GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
				GL11.glTexParameterf(data.type, GL14.GL_TEXTURE_LOD_BIAS, data.getBias());
				GL11.glTexParameterf(data.type, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f);
			}
			
			if (data.getNumRows() > 1) {
				GL11.glTexImage2D(data.type, 1, GL11.GL_RGBA, data.getWidth()/2, data.getHeight()/2, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
				GL11.glTexImage2D(data.type, 2, GL11.GL_RGBA, data.getWidth()/4, data.getHeight()/4, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
				GL11.glTexImage2D(data.type, 3, GL11.GL_RGBA, data.getWidth()/8, data.getHeight()/8, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
				GL11.glTexImage2D(data.type, 4, GL11.GL_RGBA, data.getWidth()/16, data.getHeight()/16, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
				GL11.glTexImage2D(data.type, 5, GL11.GL_RGBA, data.getWidth()/32, data.getHeight()/32, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
				//GL11.glTexImage2D(data.type, 6, GL11.GL_RGBA, data.getWidth()/12, data.getHeight()/12, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			}
		} else if (data.isNearest()) {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		} else {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		}
		
		if (data.isClampEdges()) {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		} else {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}
		GL11.glBindTexture(data.type, 0);
		return texID;
	}

	public static void unbindTexture() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
