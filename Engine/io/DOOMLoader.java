package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;

import core.Resources;
import dev.cmd.Console;
import gl.anim.Animation;
import gl.anim.component.JointTransform;
import gl.anim.component.Keyframe;

public class DOOMLoader {
private static final String VERSION = "1";
	
	public static void load(String file) {
		try {
			load(new BufferedReader(new FileReader(file)));
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	private static void load(BufferedReader reader) throws IOException {
		NestedString parent = getReaderAsString(reader);
		int numJoints = loadFileHeader(parent);
		
		for(NestedString child : parent.children) {
			loadAnimations(child, numJoints);
		}
	}
	
	private static void loadAnimations(NestedString animString, int numJoints) {
		String data = animString.data;
		String value = "";
		
		Pattern pattern = Pattern.compile("[\\w\\d\\\"]+", Pattern.MULTILINE);
		Matcher match = pattern.matcher(data);
		
		String animName = "";
		
		int numFrames;
		float framerate;
		boolean isLooping = false;
		boolean noTransition = false;
		String nextAnim = "";
		
		try {
			animName = getLabel(match, "animation");
			animName = animName.substring(1, animName.length()-1);
			
			value = getLabel(match, "numFrames");
			numFrames = Integer.parseInt(value);
			
			value = getLabel(match, "frameRate");
			framerate = Float.parseFloat(value);
			
			value = getLabel(match, "onEnd");
			
			if (value.startsWith("\"start")) {
				match.find();
				nextAnim = match.group();
				nextAnim = nextAnim.substring(0, nextAnim.length() - 1);
			}
			
			if (value.startsWith("\"loop_no_transition")) {
				noTransition = true;
				isLooping = true;
			} else if (value.startsWith("\"loop"))
				isLooping = true;
			
		} catch (NumberFormatException e) {
			throw new NumberFormatException("could not parse value: " + value);
		}
		
		Keyframe[] frames = new Keyframe[numFrames];
		
		for(int i = 0; i < numFrames; i++) {
			NestedString child = animString.children.get(i);
			frames[i] = loadFrame(child, numJoints, framerate);
		}
		
		
		Animation animation = new Animation(animName, frames[numFrames-1].getTime(), frames, numJoints, isLooping, noTransition, nextAnim); // TODO: last frame not guarenteed to be at the end
		Resources.addAnimation(animName, animation);
	}

	//public static final Quaternion CORRECTION = Quaternion.fromMatrix(new Matrix4f().rotateY(90f));
	private static Keyframe loadFrame(NestedString frameString, int numJoints, float framerate) {
		String data = frameString.data;
		String value = "";
		
		float time = Float.parseFloat(data.substring(0, data.indexOf('{')).replace("frame", ""));
		time /= framerate;
		
		Map<Byte, JointTransform> transforms = new HashMap<>();
		
		Pattern pattern = Pattern.compile("\\sframe\\s\\d+.\\d+\\s*\\{[^\\}]+", Pattern.MULTILINE);
		Matcher framesMatch = pattern.matcher(data);
		pattern = Pattern.compile("[^\\s]+", Pattern.MULTILINE);
		
		if (!framesMatch.find())
			throw new FileParseException("could not find frame " + time);
		
		String group = framesMatch.group();
		Matcher keyMatch = pattern.matcher(group);
		int start = group.indexOf('{');
		keyMatch.find(start);
		
		for(int j = 0; j < numJoints; j++) {
			try {
				Vector3f pos = new Vector3f();
				Quaternion rot = new Quaternion();
				
				keyMatch.find();
				pos.x = Float.parseFloat(keyMatch.group());
				keyMatch.find();
				pos.y = Float.parseFloat(keyMatch.group());
				keyMatch.find();
				pos.z = Float.parseFloat(keyMatch.group());

				keyMatch.find();
				rot.x = Float.parseFloat(keyMatch.group());
				keyMatch.find();
				rot.y = Float.parseFloat(keyMatch.group());
				keyMatch.find();
				rot.z = Float.parseFloat(keyMatch.group());
				keyMatch.find();
				rot.w = Float.parseFloat(keyMatch.group());
				
				JointTransform transform = new JointTransform(pos, rot);
				transforms.put((byte)j, transform);
			} catch(NumberFormatException ne) {
				throw new FileParseException("could not parse keyframe[" + time + "]");
			}
		}
		
		//JointTransform rootTransform = transforms.get((byte)0);
		//Quaternion q = rootTransform.getRotation();
		//Quaternion.mul(q, CORRECTION, q);
		
		return new Keyframe(time, transforms);
	}

	private static int loadFileHeader(NestedString meshFile) {
		String value;
		Pattern pattern = Pattern.compile("[\\w\\d\\\"]+", Pattern.MULTILINE);
		Matcher match = pattern.matcher(meshFile.data);
		
		//Model model;

		int numJoints;
		
		value = getLabel(match, "DOOM");
		if (!value.equals(VERSION))
			throw new FileParseException("Incorrect file version: " + value + ", expected " + VERSION);

		try {
			value = getLabel(match, "associatedModel");
			//model = Resources.getModel(value);
			
			value = getLabel(match, "numJoints");
			numJoints = Integer.parseInt(value);
			
		} catch (NumberFormatException e) {
			throw new NumberFormatException("could not parse value: " + value);
		}
		
		return numJoints;
	}
	
	// Helper methods
	private static NestedString getReaderAsString(BufferedReader reader) throws IOException {
		NestedString nstr = new NestedString();	
		try {
			String s = "";
			while ((s = reader.readLine()) != null) {
				if (s.endsWith("{")) {
					nstr = nstr.push();
				}
				
				else if (s.endsWith("}")) {
					nstr = nstr.pop();
				}
				
				nstr.appendln(s);
			}
		} finally {
			reader.close();
		}

		nstr.pop();
		return nstr;
	}
	
	private static String getLabel(Matcher match, final String label) throws FileParseException {
		match.find();
		String s = match.group();
		
		if (!s.equalsIgnoreCase(label))
			throw new FileParseException(label + " not found. Result: " + s);
		
		match.find();
		return match.group();
	}
}

class NestedString {
	public String data;
	
	private StringBuffer buffer = new StringBuffer();
	
	public LinkedList<NestedString> children = new LinkedList<>();
	private NestedString parent;
	
	public void appendln(String s) {
		buffer.append(s);
		buffer.append("\n");
	}

	public NestedString pop() {
		data = buffer.toString();
		buffer = null;
		return parent;
	}

	public NestedString push() {
		NestedString nstr = new NestedString();	
		nstr.parent = this;
		
		children.add(nstr);
		
		return nstr;
	}
}
