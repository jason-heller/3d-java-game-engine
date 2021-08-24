package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector4f;

import gl.Window;
import gl.res.mesh.ImageTag;
import gl.res.mesh.MeshData;
import gl.res.mesh.TextTag;
import util.Colors;

public class ModDataFileLoader {
	
	private static MeshData meshData;
	
	private static void processStructure(String key, Map<String, String> tags) {
		switch(key) {
		case "$model":
			processModelTags(tags);
			break;
		case "$text":
			addTextTag(tags);
			break;
		case "$image":
			addImageTag(tags);
			break;
		}
	}

	private static void addTextTag(Map<String, String> tags) {
		Vector3f offset = getVec3Tag(tags, "offset", new Vector3f());
		float scale = getRealTag(tags, "scale", 1f);
		String field = getStringTag(tags, "field", "");
		boolean centered = getBoolTag(tags, "centered", false);
		meshData.addTag(new TextTag(field, offset, scale, centered));
	}

	private static void addImageTag(Map<String, String> tags) {
		Vector3f offset = getVec3Tag(tags, "offset",new Vector3f());
		String field = getStringTag(tags, "field", "");
		Vector4f viewport = getVec4Tag(tags, "image_viewport", new Vector4f(0, 0, 1280, 720));
		Vector3f color = getVec3Tag(tags, "color", Colors.WHITE);
		float scale = getRealTag(tags, "scale", 1f);
		boolean centered = getBoolTag(tags, "centered", false);
		float[] uvOffset = getVecTag(tags, "image_uvoffset", new float[] { 0, 0, 1, 1 }, 4);

		viewport.mul(scale);
		
		int[] viewportArr = new int[] { (int) viewport.x, (int) viewport.y, (int) viewport.z, (int) viewport.w };
		meshData.addTag(new ImageTag(field, offset, viewportArr, uvOffset, color, centered));
	}

	private static void processModelTags(Map<String, String> tags) {
		for(String tag : tags.keySet()) {
			String value = tags.get(tag);
			
			switch(tag) {
			case "$texture":
				meshData.setDefaultTexture(value);
				break;
			case "$nolighting":
				meshData.setNoLighting(value.equals("0") ? false : true);
				break;
			}
		}
	}
	
	private static float parseReal(String value) {
		if (value.contains("/")) {
	        String[] div = value.split("/");
	        return Float.parseFloat(div[0]) / Float.parseFloat(div[1]);
	    } else {
	        return Float.parseFloat(value);
	    }
	}
	
	private static Vector3f getVec3Tag(Map<String, String> tags, String name, Vector3f nullResult) {
		String tag = tags.get("$" + name);
		return tag == null ? nullResult : parseVec3(tag);
	}
	
	private static Vector4f getVec4Tag(Map<String, String> tags, String name, Vector4f nullResult) {
		String tag = tags.get("$" + name);
		return tag == null ? nullResult : parseVec4(tag);
	}
	
	private static float[] getVecTag(Map<String, String> tags, String name, float[] nullResult, int length) {
		String tag = tags.get("$" + name);
		return tag == null ? nullResult : parseVec(tag, length);
	}
	
	private static float getRealTag(Map<String, String> tags, String name, float nullResult) {
		String tag = tags.get("$" + name);
		return tag == null ? nullResult : parseReal(tag);
	}
	
	private static String getStringTag(Map<String, String> tags, String name, String nullResult) {
		String tag = tags.get("$" + name);
		return tag == null ? nullResult : tag;
	}
	
	private static boolean getBoolTag(Map<String, String> tags, String name, boolean nullResult) {
		String tag = tags.get("$" + name);
		return tag == null ? nullResult : (tag.toLowerCase().equals("false") ? false : true);
	}
	
	private static Vector3f parseVec3(String value) {
		float[] arr = parseVec(value, 3);
		return new Vector3f(arr[0], arr[1], arr[2]);
	}
	
	private static Vector4f parseVec4(String value) {
		float[] arr = parseVec(value, 4);
		return new Vector4f(arr[0], arr[1], arr[2], arr[3]);
	}
	
	private static float[] parseVec(String value, int length) {
		String[] values = value.split(",");
		float[] v = new float[length];
		for(int i = 0; i < length; i++) {
			v[i] = parseReal(values[i].replaceAll(" ", ""));
			
		}
		return v;
	}

	static MeshData readMLD(String path) {
		File file = new File(path);
		meshData = new MeshData();
		// No such file, just carry on our business
		if (!file.exists()) {
			return MeshData.DEFUALT_DATA;
		}
		
		List<String> lines = null;
		
		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return MeshData.DEFUALT_DATA;
		}
		
		String key = null;
		Map<String, String> tags = new HashMap<>();
		
		for(String line : lines) {
			String parseableArea = null;
			int firstQuote = line.indexOf('\"');
			int lastQuote = line.lastIndexOf('\"');
			if (firstQuote != -1 && lastQuote > firstQuote) {
				parseableArea = line.substring(firstQuote + 1, lastQuote);
			} else {
				if (line.indexOf('}') != -1) {
					processStructure(key, tags);
					key = null;
					tags.clear();
				}
				continue;
			}
			
			if (key == null) {
				key = parseableArea;
			} else {
				String[] params = parseableArea.split("\"");
				
				if (params.length == 1) {
					tags.put(params[0], "1");
				} else if (params.length > 2) {
					tags.put(params[0], params[2]);
				}
			}
		}
		
		return meshData;
	}
}
