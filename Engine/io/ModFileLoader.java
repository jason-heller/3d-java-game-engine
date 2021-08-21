package io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.anim.component.Joint;
import gl.anim.component.Skeleton;
import gl.res.Model;
import gl.res.mesh.MeshData;

public class ModFileLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports

	private static Model extractModelData(String key, DataInputStream is, boolean hasNavMesh) throws IOException {
		final int vertexCount = is.readInt();
		final int indexCount = is.readInt();

		// Mesh data
		final float[] vertices = new float[vertexCount * 3];
		final float[] uvs = new float[vertexCount * 2];
		final float[] normals = new float[vertexCount * 3];
		final int[] indices = new int[indexCount];
		final int[] jointIds = new int[vertexCount * 3];
		final float[] weights = new float[vertexCount * 3];
		// float furthestPoint = is.readFloat();
		//////

		int i;
		for (i = 0; i < vertexCount; i++) {
			vertices[i * 3 + 0] = is.readFloat();
			vertices[i * 3 + 1] = is.readFloat();
			vertices[i * 3 + 2] = is.readFloat();

			/*
			 * vertices[(i*3)+2] = -is.readFloat(); vertices[(i*3)+1] = is.readFloat();
			 * vertices[(i*3)+0] = -is.readFloat();
			 */
			uvs[i * 2 + 0] = is.readFloat();
			uvs[i * 2 + 1] = is.readFloat();
			normals[i * 3 + 0] = is.readFloat();
			normals[i * 3 + 1] = is.readFloat();
			normals[i * 3 + 2] = is.readFloat();

			jointIds[i * 3 + 0] = is.readByte();
			jointIds[i * 3 + 1] = is.readByte();
			jointIds[i * 3 + 2] = is.readByte();
			weights[i * 3 + 0] = is.readFloat();
			weights[i * 3 + 1] = is.readFloat();
			weights[i * 3 + 2] = is.readFloat();

			// indices[i] = is.readInt();
		}

		for (i = 0; i < indexCount; i++) {
			indices[i] = is.readInt();
		}

		final Vector3f max = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		final Vector3f min = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());

		final byte numJoints = is.readByte();

		// Resources.addModel("", path);
		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.createAttribute(3, jointIds, 3);
		model.createAttribute(4, weights, 3);
		model.createIndexBuffer(indices);
		model.setSkeleton(new Skeleton(numJoints, getJoints(is)));
		model.unbind();

		model.setBounds(max, min);

		final byte numAnimations = is.readByte();
		
		for (int a = 0; a < numAnimations; a++) {
			AniFileLoader.extractAnimationData(key, is);
		}

		Vector3f[] navMesh = null;
		if (hasNavMesh) {
			int numVerts = is.readShort() * 3;
			navMesh = new Vector3f[numVerts];
			for(int j = 0; j < numVerts; j++) {
				navMesh[j] = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
			}
		}
		
		if (hasNavMesh) {
			model.setNavMesh(navMesh);
		}
		
		return model;
	}

	private static Model extractStaticModelData(DataInputStream is, boolean hasNavMesh) throws IOException {
		final int vertexCount = is.readInt();
		final int indexCount = is.readInt();

		// Mesh data
		final float[] vertices = new float[vertexCount * 3];
		final float[] uvs = new float[vertexCount * 2];
		final float[] normals = new float[vertexCount * 3];
		final int[] indices = new int[indexCount];
		//////

		int i;
		for (i = 0; i < vertexCount; i++) {
			vertices[i * 3 + 0] = is.readFloat();
			vertices[i * 3 + 1] = is.readFloat();
			vertices[i * 3 + 2] = is.readFloat();
			uvs[i * 2 + 0] = is.readFloat();
			uvs[i * 2 + 1] = is.readFloat();
			normals[i * 3 + 0] = is.readFloat();
			normals[i * 3 + 1] = is.readFloat();
			normals[i * 3 + 2] = is.readFloat();

			indices[i] = is.readShort();
		}

		for (; i < indexCount; i++) {
			indices[i] = is.readShort();
		}

		final Vector3f max = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		final Vector3f min = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		
		Vector3f[] navMesh = null;
		if (hasNavMesh) {
			int numVerts = is.readShort() * 3;
			navMesh = new Vector3f[numVerts];
			for(int j = 0; j < numVerts; j++) {
				navMesh[j] = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
			}
		}

		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.createIndexBuffer(indices);
		model.unbind();

		model.setBounds(max, min);
		
		if (hasNavMesh) {
			model.setNavMesh(navMesh);
		}

		return model;
	}

	private static Joint getJoints(DataInputStream is) throws IOException {
		final byte id = is.readByte();
		final String name = FileUtils.readString(is);
		final Matrix4f matrix = FileUtils.readMatrix4f(is);

		final Joint joint = new Joint(id, name, matrix);
		final byte numChildren = is.readByte();
		for (int i = 0; i < numChildren; i++) {
			joint.addChild(getJoints(is));
		}

		return joint;
	}

	public static Model readModFile(String key, byte[] data) {
		return readModFile(key, new DataInputStream(new ByteArrayInputStream(data)));
	}

	/**
	 * Load a .MOD file
	 *
	 * @param path           the to the file's directory within the res folder, for
	 *                       example, setting this to "weps/gun.mod" would point to
	 *                       a file called "gun.mod" in the res/weps folder
	 * @param saveVertexData setting this to true bakes the vertex data into the
	 *                       model
	 * @return
	 */
	public static Model readModFile(String key, String path) {
		try {
			String fullPath = "src/res/" + path;
			Model model = readModFile(key, new DataInputStream(new FileInputStream(fullPath)));
			MeshData meshData = ModDataFileLoader.readMLD(fullPath.substring(0, fullPath.lastIndexOf('.')) + ".mld");
			model.setMeshData(meshData);
			return model;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Model readModFile(String key, DataInputStream is) {
		Model model = null;

		try {

			// Header
			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();
			final byte flags = is.readByte();

			if (version != EXPECTED_VERSION) {
				return null;
			}

			if (!fileExtName.equals("MOD")) {
				return null;
			}

			boolean hasNavMesh = (flags & 0x02) != 0;
			
			model = (flags & 0x01 << 0) == 1 ? extractModelData(key, is, hasNavMesh)
					: extractStaticModelData(is, hasNavMesh);

		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		return model;
	}

}
