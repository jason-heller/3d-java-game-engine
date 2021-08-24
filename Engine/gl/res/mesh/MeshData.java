package gl.res.mesh;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.joml.Matrix4f;

import gl.res.Model;
import gl.res.Texture;

public class MeshData {
	private static Map<String, String> fields = new HashMap<>();
	
	private String defaultTexture = "default";
	private boolean noLighting;

	public static final MeshData DEFUALT_DATA = new MeshData();
	
	private LinkedList<MeshTag> structs = new LinkedList<>();
	
	public String getDefaultTexture() {
		return defaultTexture;
	}

	public void setDefaultTexture(String defaultTexture) {
		this.defaultTexture = defaultTexture;
	}

	public LinkedList<MeshTag> getTags() {
		return structs;
	}

	public void addTag(MeshTag struct) {
		this.structs.add(struct);
	}

	public void setNoLighting(boolean noLighting) {
		this.noLighting = noLighting;
	}
	
	public boolean hasNoLighting() {
		return noLighting;
	}

	public void update(Model model, Texture texture, Matrix4f matrix) {
		for(MeshTag struct : structs) {
			struct.update(model, texture, matrix);
		}
	}

	public static String getField(String field) {
		return fields.get(field);
	}
	
	public static void setField(String field, String value) {
		fields.put(field, value);
	}
	
	public static String removeField(String field) {
		return fields.remove(field);
	}
}