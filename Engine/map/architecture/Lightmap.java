package 
map.architecture;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import core.Resources;
import dev.cmd.Console;
import gl.Render;
import gl.res.Texture;
import gl.res.TextureUtils;
import map.architecture.components.ArcFace;

public class Lightmap {
	//private List<Integer> lightmap;
	private Texture texture;
	private static LMNode rootNode;
	private static final int SIZE = 1024;
	private boolean active;
	
	public static int[] filteringQualities = new int[] {GL11.GL_NEAREST, GL11.GL_LINEAR};
	
	public Lightmap() {
		//lightmap = new ArrayList<Integer>();
		rootNode = new LMNode(0,0,SIZE,SIZE);
		
        byte[] rgba = new byte[4*SIZE*SIZE];
        for(int i = 0; i < rgba.length; i+=4) {
        	rgba[i] = 0;
        	rgba[i+1] = 0;
        	rgba[i+2] = 0;
        	rgba[i+3] = -1;
        }

       // buf.flip();
        texture = TextureUtils.createTexture(rgba, (byte)0, SIZE, SIZE, false);
        //texture.bind(0);
        Resources.addTexture("lightmap", texture);
        setFiltering(Render.shadowQuality);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        
        //GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        // Set the last few pixels to white (for non-lightmapped faces)

    	byte[] whitePixels = new byte[] {
    			-1, -1, -1, -1,
    			-1, -1, -1, -1,
    			-1, -1, -1, -1,
    			-1, -1, -1, -1
    	};
    	ByteBuffer buf;
    	texture.bind(0);
    	buf = BufferUtils.createByteBuffer(whitePixels.length);
    	buf.put(whitePixels);
    	buf.flip();
    	GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, SIZE-2, SIZE-2, 2, 2, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
	}

	public void setFiltering(int filter) {
		texture.bind(0);
		int quality = Math.min(Render.shadowQuality, 1);
		int f = filteringQualities[quality];
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, f);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, f);// _MIPMAP_LINEAR
	}

	public void create(byte[] lighting, ArcFace[] faces) {
		active = true;
		for (ArcFace face : faces) {
			int width = (int)face.lmSizes[0] + 1;
			int height = (int)face.lmSizes[1] + 1;
			
			if(height <= 0 || width <= 0) { continue; }
			
			if (face.lmIndex == -1) {
				face.lightmapOffsetX = 1;
                face.lightmapOffsetY = 1;
                face.lightmapScaleX = 1 / (float)SIZE;
                face.lightmapScaleY = 1 / (float)SIZE;
				continue;
			}
			
			byte[] styles = face.lmStyles;
			int numStyles;
			for (numStyles = 0; numStyles < styles.length; numStyles++) {
				if (styles[numStyles] == -1)
					break;
			}
			LMNode node = allocateRect(width+2, height+2, null);

			if (node != null) {
				int byteCount = width * height * 4;
				int borderedByteCount = (width + 2) * (height + 2) * 4; // includes border
				int rowBytes = (width + 2) * 4;
				int[] lightmap = new int[borderedByteCount];
				for (int j = 0; j < numStyles; ++j) {
					int lightOffset = face.lmIndex + (byteCount * j);
					int[] lightbuffer = subarray(lighting, lightOffset, lightOffset + byteCount); // byte
					byte[] expbuffer = subarrayChar(lighting, lightOffset + 3, lightOffset + byteCount); // Exponent (char)

					int k = 0;

					// Fill out the lightmap, minus borders
					for (int y = 0; y < height; ++y) {
						int o = (rowBytes * (y + 1)) + 4;
						for (int x = 0; x < width; ++x) {
							float exp = (float) Math.pow(2, expbuffer[k]);
							lightmap[o] = clamp(lightmap[o] + ((lightbuffer[k]) * exp));++k;++o;
							lightmap[o] = clamp(lightmap[o] + ((lightbuffer[k]) * exp));++k;++o;
							lightmap[o] = clamp(lightmap[o] + ((lightbuffer[k]) * exp));++k;++o;
							lightmap[o] = 255;++k;++o;
						}
					}

					// Generate the borders
					lightmap = fillBorders(lightmap, width + 2, height + 2);
				}
				
				texture.bind(0);
				ByteBuffer buf = BufferUtils.createByteBuffer(lightmap.length);
				for(int i = 0; i < lightmap.length; i++) {
					buf.put(intToByte(lightmap[i]));
				}
				buf.flip();
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, (int)node.x, (int)node.y, width+2, height+2, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);

				face.lightmapOffsetX = ((int)node.x+1) / (float)SIZE;
                face.lightmapOffsetY = ((int)node.y+1) / (float)SIZE;
                face.lightmapScaleX = width / (float)SIZE;
                face.lightmapScaleY = height / (float)SIZE;
                
        		
			} else {
				Console.log("#rLIGHTMAP TOO BIG");
			}
		}
		
		/*ByteBuffer buf = ByteBuffer.allocateDirect(lightmapData.length);
		buf.order(ByteOrder.nativeOrder());
		buf.put(lightmapData, 0, lightmapData.length);
		buf.flip();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		lightmap = GL11.glGenTextures();
		this.width = width;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightmap);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, width, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);*/
	}
	
	private static int clamp(float value) {
		return (int) (value > 255 ? 255 : (value < 0 ? 0 : value));
	}

	private byte intToByte(int i) {
		return (byte)(i>127?i-256:i);
	}
	
	private static int byteToInt(int i) {
		byte b = (byte)i;
		return (b<0)?256+b:b;
	}

	public Texture getLightmap() {
		return texture;
	}

	public int getWidth() {
		return SIZE;
	}
	
	public void bind(int i) {
		texture.bind(i);
	}
	
	public void delete() {
		//GL11.glDeleteTextures(texture);
		texture.delete();
		active = false;
	}
	
	private static LMNode allocateRect(int width, int height, LMNode node) {
        if(node == null) { node = rootNode; }
        
        // Check children node
        if(node.nodes != null) { 
            LMNode retNode = allocateRect(width, height, node.nodes[0]);
            if(retNode != null) { return retNode; }
            return allocateRect(width, height, node.nodes[1]);
        }

        // Already used
        if(node.filled) { return null; }

        // Too small
        if(node.width < width || node.height < height) { return null; }

        // Perfect fit. Allocate without splitting
        if(node.width == width && node.height == height) {
            node.filled = true;
            return node;
        }

        // We need to split if we've reached here
        LMNode[] nodes;

        // Which way do we split?
        if ((node.width - width) > (node.height - height)) {
            nodes = new LMNode[] {
            		new LMNode(node.x,node.y,width,node.height),
            		new LMNode(node.x+width,node.y,node.width-width,node.height)
            };
        } else {
        	nodes = new LMNode[] {
            		new LMNode(node.x,node.y,node.width,height),
            		new LMNode(node.x,node.y+height,node.width,node.height-height)
            };
        }
        node.nodes = nodes;
        return allocateRect(width, height, node.nodes[0]);
    }
	
	private static int[] fillBorders(int[] lightmap, int width, int height) {
        int rowBytes = width * 4;
        int o;
        
        // Fill in the sides
        for(int y = 1; y < height-1; ++y) {
            // left side
            o = rowBytes * y;
            lightmap[o] = lightmap[o + 4]; ++o;
            lightmap[o] = lightmap[o + 4]; ++o;
            lightmap[o] = lightmap[o + 4]; ++o;
            lightmap[o] = lightmap[o + 4];
            
            // right side
            o = (rowBytes * (y+1)) - 4;
            lightmap[o] = lightmap[o - 4]; ++o;
            lightmap[o] = lightmap[o - 4]; ++o;
            lightmap[o] = lightmap[o - 4]; ++o;
            lightmap[o] = lightmap[o - 4];
        }
        
        int end = width * height * 4;
        
        // Fill in the top and bottom
        for(int x = 0; x < rowBytes; ++x) {
            lightmap[x] = lightmap[x + rowBytes];
            lightmap[(end-rowBytes) + x] = lightmap[(end-(rowBytes*2) + x)];
        }
        
        return lightmap;
	}
	
	/*private static byte clamp(int value) {
		return (byte) (value > 127 ? 127 : (value < -128 ? -128 : value));
	}*/

	private static int[] subarray(byte[] arr, int start, int end) {
		int len = end-start;
		int[] newArr = new int[len];
		for (int i = 0; i < len; i++) {
			newArr[i] = byteToInt(arr[start + i]);
		}
		return newArr;
	}

	private static byte[] subarrayChar(byte[] arr, int start, int end) {
		int len = end-start;
		byte[] newArr = new byte[len];
		for (int i = 0; i < len; i++) {
			newArr[i] = arr[start + i];
		}
		return newArr;
	}
	
	public boolean isActive() {
		return active;
	}
}

class LMNode {
	public int x, y, width, height;
	public LMNode[] nodes;
	public boolean filled = false;
	public LMNode(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}