package io;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joml.Quaternion;
import org.joml.Vector3f;

import gl.anim.Animation;
import gl.anim.component.JointTransform;
import gl.anim.component.Keyframe;

/**
 * A very quick and crappy .md5anim loader. Didn't bother to handle the baseframe, bounds, and expects a W component for everything
 * Just a stop-gap between this and .DOOM files
 *
 */
public class MD5AnimLoader {
	
	public static Animation load(String file) {
		try {
			return load(new BufferedReader(new FileReader(file)));
		} catch (IOException e) { 
			e.printStackTrace();
			return null;
		}
	}
	
	private static Animation load(BufferedReader reader) throws IOException {
		String animFile = getReaderAsString(reader);
		
		Animation animation = loadHeader(animFile);
		
		JointTransform[] baseframe = new JointTransform[animation.getNumJoints()];
		
		MD5AnimJointData[] jointData = loadJoints(animFile, animation);
		loadBaseFrame(animFile, animation, baseframe);
		loadKeyframes(animFile, animation, baseframe, jointData);
		
		return animation;
	}

	private static Animation loadHeader(String meshFile) {
		String value;
		Pattern pattern = Pattern.compile("[\\w\\d\\\"]+", Pattern.MULTILINE);
		Matcher match = pattern.matcher(meshFile.substring(0, 200));
		
		Keyframe[] frames;
		Animation anim;
		int numFrames;
		int numJoints;
		// int numAnimComponents;
		float duration;
		
		value = getLabel(match, "md5version");
		if (!value.equals("10"))
			throw new FileParseException("Can only parse V10, file is version " + value);

		try {
			value = getLabel(match, "commandline");	// Ignoring this
			
			value = getLabel(match, "numFrames");
			numFrames = Integer.parseInt(value);
			frames = new Keyframe[numFrames];
			
			value = getLabel(match, "numJoints");
			numJoints = Integer.parseInt(value);
			
			value = getLabel(match, "frameRate");
			duration = (1f / Float.parseFloat(value)) * numFrames;
			
			value = getLabel(match, "numAnimatedComponents");		// is the number of parameters per frame used to compute the frame skeletons. These parameters, combined with the baseframe skeleton given in the MD5 Anim file, permit to build a skeleton for each frame.
			// numAnimComponents = Integer.parseInt(value);
			
			anim = new Animation(duration, frames, numJoints);
			
		} catch (NumberFormatException e) {
			throw new NumberFormatException("could not parse value: " + value);
		}
		
		return anim;
	}
	
	private static MD5AnimJointData[] loadJoints(String meshFile, Animation anim) {
		Pattern pattern = Pattern.compile("hierarchy\\s+\\{[^\\}]+\\}", Pattern.MULTILINE);
		Matcher match = pattern.matcher(meshFile);

		if (!match.find())
			throw new FileParseException("No joints struct found");

		String joints = match.group();
		pattern = Pattern.compile("//[\\s\\w\\d.]+", Pattern.MULTILINE);
		match = pattern.matcher(joints);
		joints = match.replaceAll("");

		pattern = Pattern.compile("[^\\s()]+", Pattern.MULTILINE);
		match = pattern.matcher(joints);
		int start = joints.indexOf("{");
		match.find(start);
		
		// String name = null;
		// int parent;
		int flags;		// XYZABC, XYZ = update pos coords if set, ABC = update quaternion coords if set (indexed by startOffset), otherwise use base frame
		int startOffset;
		
		MD5AnimJointData[] jointData = new MD5AnimJointData[anim.getNumJoints()];
		
		for (int i = 0; i < anim.getNumJoints(); i++) {
			try {
				jointData[i] = new MD5AnimJointData();
				
				match.find();
				jointData[i].name = match.group();
				match.find();
				jointData[i].parent = Integer.parseInt(match.group());

				match.find();
				flags = Integer.parseInt(match.group());
				match.find();
				startOffset = Integer.parseInt(match.group());

			} catch(NumberFormatException e) {
				throw new FileParseException("could not parse joint[" + i + "]");
			}

		}
		
		return jointData;
	}
	
	private static void loadBaseFrame(String animFile, Animation anim, JointTransform[] baseframe) {
		Pattern pattern = Pattern.compile("baseframe\\s+\\{[^\\}]+\\}", Pattern.MULTILINE);
		Matcher match = pattern.matcher(animFile);
		
		if (!match.find())
			throw new FileParseException("No baseframe struct found");
		
		String group = match.group();
		pattern = Pattern.compile("[^\\s()]+", Pattern.MULTILINE);
		match = pattern.matcher(group);
		match.find();
		match.find();
		
		for(int i = 0; i < anim.getNumJoints(); i++) {
			try {
				Vector3f pos = new Vector3f();
				Quaternion rot = new Quaternion();
				
				match.find();
				pos.x = Float.parseFloat(match.group());
				match.find();
				pos.y = Float.parseFloat(match.group());
				match.find();
				pos.z = Float.parseFloat(match.group());
				
				match.find();
				rot.x = Float.parseFloat(match.group());
				match.find();
				rot.y = Float.parseFloat(match.group());
				match.find();
				rot.z = Float.parseFloat(match.group());
				match.find();
				rot.w = Float.parseFloat(match.group());
				
				baseframe[i] = new JointTransform(pos, rot);
			} catch(NumberFormatException ne) {
				throw new FileParseException("could not parse joint[" + i + "]");
			}
		}
	}
	
	// TODO: flags and startOffset index into this, this only works if flags are all "63" (all set) and startOffset increases by 6 each line
	
	// in md5 animations, the joints position and rotations are relatives to their parents
	private static void loadKeyframes(String animFile, Animation anim, JointTransform[] baseframe, MD5AnimJointData[] jointData) {
		Pattern pattern = Pattern.compile("frame\\s\\d+\\s*\\{[^\\}]+\\}", Pattern.MULTILINE);
		Matcher framesMatch = pattern.matcher(animFile);
		pattern = Pattern.compile("[^\\s]+", Pattern.MULTILINE);
		
		Keyframe[] keyframes = anim.getKeyframes();
		int numFrames = keyframes.length;
		
		float time = 0f;
		float frameTime = anim.getDuration() / anim.getKeyframes().length;
		
		for (int i = 0; i < numFrames; i++) {
			
			if (!framesMatch.find())
				throw new FileParseException("could not find frame " + i);
			
			Map<Byte, JointTransform> transforms = new HashMap<>();
			
			String group = framesMatch.group();
			Matcher keyMatch = pattern.matcher(group);
			int start = group.indexOf('{');
			keyMatch.find(start);
			
			for(int j = 0; j < anim.getNumJoints(); j++) {
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
					throw new FileParseException("could not parse keyframe[" + i + "]");
				}
			}
			
			keyframes[i] = new Keyframe(time, transforms);
			time += frameTime;
		}
	}
	
	// Helper methods
	public static String getReaderAsString(BufferedReader reader) throws IOException {
		StringBuffer buffer = new StringBuffer();	
		try {
			String s = "";
			while ((s = reader.readLine()) != null) {
				buffer.append(s);
				buffer.append("\n");
			}
		} finally {
			reader.close();
		}
		
		String file = buffer.toString();
		return file;
	}
	
	public static String getLabel(Matcher match, final String label) throws FileParseException {
		match.find();
		String s = match.group();
		
		if (!s.equalsIgnoreCase(label))
			throw new FileParseException(label + " not found. Result: " + s);
		
		match.find();
		return match.group();
	}
}

class MD5AnimJointData {
	public String name;
	public int parent;
}


