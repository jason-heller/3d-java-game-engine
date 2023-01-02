package io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

import org.joml.Quaternion;
import org.joml.Vector3f;

import core.Resources;
import dev.cmd.Console;
import gl.anim.component.Joint;
import gl.anim.component.Skeleton;
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import gl.res.TextureUtils;

public class MFLoader {
	public static final byte VERSION_MAJOR = 3;
	public static final byte VERSION_MINOR = 0;
	private static final int MAX_BONES_PER_VERTEX = 3;

	private static Model getMeshes(ByteBuffer is) throws IOException, NegativeArraySizeException {
		
		int flags = is.get();
		int numMeshes = is.get();
		
		boolean hasArmature = (flags & 1) == 1;
		
		Model model = new Model(numMeshes);
		Skeleton skeleton = null;
		
		for(int i = 0; i < numMeshes; i++) {
			
			model.setMesh(i, getMesh(is));
			
			Texture texture = TextureUtils.createTexture("res/models/" + model.getMeshes()[i].name.toLowerCase() + ".png");		// Hacky hacky hacky
			model.setTexture(i, texture);
		}
		
		if (hasArmature)
			skeleton = getSkeleton(is);

		model.setSkeleton(skeleton);

		return model;
	}
	
	private static Mesh getMesh(ByteBuffer bb) throws IOException, NegativeArraySizeException {
		
		String name = "";
		byte c;
		while((c = bb.get()) != 0) {
			name += (char)c;
		}

		final int vertexCount = bb.getInt();
		final int indexCount = bb.getInt();
		
		final int NUM_VERTEX_DATA = vertexCount * 3;
		final int NUM_UV_DATA = vertexCount * 2;
		final int NUM_BONE_DATA = vertexCount * MAX_BONES_PER_VERTEX;

		// Mesh data
		final float[] vertices 	= new float[NUM_VERTEX_DATA];
		final float[] uvs 		= new float[NUM_UV_DATA];
		final float[] normals 	= new float[NUM_VERTEX_DATA];
		final float[] weights 	= new float[NUM_BONE_DATA];
		final int[] boneIds 	= new int[NUM_BONE_DATA];
		final int[] indices		= new int[indexCount];
		//////

		int i;
		for (i = 0; i < NUM_VERTEX_DATA; i++)
			vertices[i] = bb.getFloat();
		
		for (i = 0; i < NUM_UV_DATA; i += 2) {
			uvs[i] = bb.getFloat();
			uvs[i+1] = 1f - bb.getFloat();		// FUCK
		}
		
		for (i = 0; i < NUM_VERTEX_DATA; i++)
			normals[i] = bb.getFloat();
		
		for (i = 0; i < NUM_BONE_DATA; i++)
			weights[i] = bb.getFloat();
		
		for (i = 0; i < NUM_BONE_DATA; i++)
			boneIds[i] = bb.get();
		
		for (i = 0; i < indexCount; i++)
			indices[i] = bb.getShort();

		Mesh mesh = Mesh.create();
		mesh.name = name;
		mesh.defaultTexture = name;
		
		mesh.bind();
		mesh.createAttribute(0, vertices, 3);
		mesh.createAttribute(1, uvs, 2);
		mesh.createAttribute(2, normals, 3);
		mesh.createAttribute(3, boneIds, MAX_BONES_PER_VERTEX);
		mesh.createAttribute(4, weights, MAX_BONES_PER_VERTEX);
		mesh.createIndexBuffer(indices);
		mesh.unbind();

		return mesh;
	}

	private static Skeleton getSkeleton(ByteBuffer bb) throws IOException {
		final int numBones = bb.get();

		String[] boneNames = new String[numBones];
		byte[] boneParents = new byte[numBones];
		Vector3f[] boneLocations = new Vector3f[numBones];
		Quaternion[] boneRotations = new Quaternion[numBones];
		
		byte c;
		for(int i = 0; i < numBones; i++) {
			boneNames[i] = "";
			while((c = bb.get()) != 0)
				boneNames[i] += (char)c;
		}
		
		for(int i = 0; i < numBones; i++) {
			boneParents[i] = bb.get();
		}

		for(int i = 0; i < numBones; i++) {
			Vector3f bonePos = new Vector3f(bb.getFloat(), bb.getFloat(), bb.getFloat());		// Pos/Rot of bone in object local space;
			Quaternion boneRot = new Quaternion(bb.getFloat(), bb.getFloat(), bb.getFloat(), bb.getFloat());
			boneLocations[i] = bonePos;
			boneRotations[i] = boneRot;
		}
		
		Joint root = createJointHierarchy(boneParents, boneNames, boneLocations, boneRotations);
		
		return new Skeleton(numBones, root);
	}

	private static Joint createJointHierarchy(byte[] boneParents, String[] boneNames, Vector3f[] boneLocations, Quaternion[] boneRotations) {
		int numBones = boneParents.length;
		Joint[] joints = new Joint[numBones];
	
		for(byte i = 0; i < numBones; i++) {
			joints[i] = new Joint(i, boneNames[i], boneLocations[i], boneRotations[i]);
		}
		
		for(int i = 0; i < numBones; i++) {
			for(int j = i + 1; j < numBones; j++) {
				if (boneParents[j] == i) {
					joints[i].addChild(joints[j]);
				}
			}
		}

		return joints[0];
	}

	/**
	 * Load a .MD file
	 *
	 * @param path           the to the file's directory within the res folder, for
	 *                       example, setting this to "weps/gun.mod" would point to
	 *                       a file called "gun.md" in the res/weps folder
	 * @param saveVertexData setting this to true bakes the vertex data into the
	 *                       model
	 * @return
	 */
	public static Model readMF(String key, String path) {
		try {
			String fullPath = "src/res/" + path;
			
			File file = new File(fullPath);
			
			if (!file.exists()) {
				Console.severe("Missing file " + path);
				return Resources.ERROR;
			}
			
			byte[] byteArray= Files.readAllBytes(file.toPath());
			ByteBuffer bb = ByteBuffer.wrap(byteArray);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			Model model = readMF(key, bb);

			return model;
		} catch (final IOException e) {
			e.printStackTrace();
			return Resources.ERROR;
		}
	}
	
	private static Model readMF(String key, ByteBuffer bb) {
		Model model = null;

		try {

			// Header
			String fileExtName = "";
			for(int i = 0; i < 6; i++) 
				fileExtName += (char)bb.get();
			
			if (!fileExtName.equals("ANIMDL"))
				return Resources.ERROR;

			int major = bb.get();
			int minor = bb.get();
			
			if (major != VERSION_MAJOR || minor != VERSION_MINOR)
				return Resources.ERROR;

			model = getMeshes(bb);

		} catch (final IOException e) {
			System.err.println("MF file " + key + " failed to load. Read " + bb.position() + " bytes");
			e.printStackTrace();
		} catch (final NegativeArraySizeException e) {
			System.err.println("MF file " + key + " failed to be read; loader expected different values. Read " + bb.position() + " bytes");
			e.printStackTrace();
		}
		
		return model;
	}

}
