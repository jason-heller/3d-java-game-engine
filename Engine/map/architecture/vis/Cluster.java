package map.architecture.vis;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import gl.res.Mesh;
import map.architecture.components.ArcFace;
import scene.entity.util.LightStyle;

public class Cluster {
	
	private int diffuseId;
	private int bumpMapId = -1;
	private int specMapId = -1;
	private FloatBuffer lmOffsets = null;
	private LightStyle[] styles = null;
	private Mesh model;

	public Cluster(Mesh model, int diffuseId, ArcFace face) {
		float[] uOffsets = face.lightmapOffsetX;
		float[] vOffsets = face.lightmapOffsetY;
		byte[] faceStyles = face.lmStyles;
		
		this.model = model;
		this.diffuseId = diffuseId;
		
		if (faceStyles[1] == -1)
			return;
		
		lmOffsets = BufferUtils.createFloatBuffer(9);
		
		if (uOffsets == null)
			return;

		this.styles = new LightStyle[uOffsets.length - 1];
		for(int i = 1; i < uOffsets.length; i++) {
			int styleIndex = i - 1;
			this.styles[styleIndex] = LightStyle.getStyleFromId(faceStyles[i]);
			lmOffsets.put((styleIndex * 3), uOffsets[i] - uOffsets[0]);
			lmOffsets.put(1 + (styleIndex * 3), vOffsets[i] - vOffsets[0]);
			lmOffsets.put(2 + (styleIndex * 3), 1f);
		}
		
		//lmOffsets.flip();
	}

	public Mesh getModel() {
		return model;
	}
	
	public int getDiffuseId() {
		return diffuseId;
	}

	public int getBumpMapId() {
		return bumpMapId;
	}

	public void setBumpMapId(int bumpMapId) {
		this.bumpMapId = bumpMapId;
	}

	public int getSpecMapId() {
		return specMapId;
	}
	
	public void setSpecMapId(int specMapId) {
		this.specMapId = specMapId;
	}

	public FloatBuffer getTextureOffsets() {
		return lmOffsets;
	}

	public boolean hasAlternativeStyles() {
		return styles != null;
	}

	public void setAlpha(LightStyle style, float alpha) {
		for(int i = 0; i < styles.length; i++) {
			if (styles[i] == style) {
				lmOffsets.put(i * 3 + 2, alpha);
				break;
			}
		}
	}
}
