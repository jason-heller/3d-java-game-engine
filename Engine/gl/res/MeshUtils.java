package gl.res;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import io.FileUtils;

public class MeshUtils {
	public static Mesh buildMesh(float[] vertices, float[] uvs, float[] normals, int[] indices) {
		final Mesh mesh = Mesh.create();
		mesh.bind();
		mesh.createAttribute(0, vertices, 3);
		mesh.createAttribute(1, uvs, 2);
		mesh.createAttribute(2, normals, 3);
		mesh.createIndexBuffer(indices);
		mesh.unbind();

		return mesh;
	}

	public static Mesh buildVao(List<Float> meshVerts, List<Float> meshUvs, List<Float> meshNorms,
			List<Integer> meshIndices) {
		final int len = meshVerts.size() / 3;
		final float[] verts = new float[meshVerts.size()];
		final float[] uvs = new float[meshUvs.size()];
		final float[] norms = new float[meshNorms.size()];
		final int[] inds = new int[meshIndices.size()];
		for (int i = 0; i < len; i++) {
			verts[i * 3 + 0] = meshVerts.get(i * 3 + 0);
			verts[i * 3 + 1] = meshVerts.get(i * 3 + 1);
			verts[i * 3 + 2] = meshVerts.get(i * 3 + 2);

			uvs[i * 2 + 0] = meshUvs.get(i * 2 + 0);
			uvs[i * 2 + 1] = meshUvs.get(i * 2 + 1);

			norms[i * 3 + 0] = meshNorms.get(i * 3 + 0);
			norms[i * 3 + 1] = meshNorms.get(i * 3 + 1);
			norms[i * 3 + 2] = meshNorms.get(i * 3 + 2);
		}

		for (int i = 0; i < meshIndices.size(); i++) {
			inds[i] = meshIndices.get(i);
		}

		return buildMesh(verts, uvs, norms, inds);
	}

	public static Mesh buildBillboardMesh() {
		final Mesh mesh = Mesh.create();
		mesh.bind();
		mesh.createAttribute(0, new float[] { -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f }, 2);
		mesh.createAttribute(1, new float[] { 0, 1, 1, 1, 0, 0, 1, 0 }, 2);
		mesh.unbind();
		return mesh;
	}
	
	@Deprecated
	public static Mesh loadObj(String path) {

		final List<float[]> vertices = new ArrayList<float[]>();
		final List<float[]> uvs = new ArrayList<float[]>();
		final List<float[]> normals = new ArrayList<float[]>();

		final List<int[]> indices = new ArrayList<int[]>();
		final List<Byte> boneIndices = new ArrayList<Byte>();
		final List<Integer> indexOrder = new ArrayList<Integer>();
		final Vector3f max = new Vector3f(-1000000, -1000000, -1000000);
		final Vector3f min = new Vector3f(1000000, 1000000, 1000000);

		boolean hasTexture = false;

		BufferedReader reader;

		try {
			reader = FileUtils.getReader(path);
			String line = reader.readLine();

			while (line != null) {
				final String[] data = line.split(" ");

				if (data.length > 2) {
					if (data[0].equals("v")) {
						final float[] vertex = new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
								Float.parseFloat(data[3]) };
						vertices.add(vertex);
						min.x = Math.min(min.x, vertex[0]);
						min.y = Math.min(min.y, vertex[1]);
						min.z = Math.min(min.z, vertex[2]);
						max.x = Math.max(max.x, vertex[0]);
						max.y = Math.max(max.y, vertex[1]);
						max.z = Math.max(max.z, vertex[2]);
					} else if (data[0].equals("vt")) {
						final float[] uvCoord = new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]) };
						hasTexture = true;
						uvs.add(uvCoord);
					} else if (data[0].equals("vn")) {
						final float[] normal = new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
								Float.parseFloat(data[3]) };
						normals.add(normal);
					} else if (data[0].equals("f")) {
						if (data.length > 4) {
							final int[] curIndices = new int[4];

							for (byte i = 1; i < data.length; i++) {
								final String[] faceData = data[i].split("/");

								int[] index;
								if (hasTexture) {
									index = new int[] { Integer.parseInt(faceData[0]) - 1,
											Integer.parseInt(faceData[1]) - 1, Integer.parseInt(faceData[2]) - 1 };
								} else {
									index = new int[] { Integer.parseInt(faceData[0]) - 1, 0,
											Integer.parseInt(faceData[2]) - 1 };
								}

								int indexPosition = -1;

								for (int j = 0; j < indices.size(); j++) {
									final int[] check = indices.get(j);
									if (check[0] == index[0] && check[1] == index[1] && check[2] == index[2]) {
										indexPosition = j;
										break;
									}
								}

								if (indexPosition == -1) {
									indices.add(index);
									curIndices[i - 1] = indices.size() - 1;
								} else {
									curIndices[i - 1] = indexPosition;
								}
							}

							indexOrder.add(curIndices[0]);
							indexOrder.add(curIndices[1]);
							indexOrder.add(curIndices[3]);
							indexOrder.add(curIndices[3]);
							indexOrder.add(curIndices[1]);
							indexOrder.add(curIndices[2]);
						} else {
							for (byte i = 1; i < data.length; i++) {
								final String[] faceData = data[i].split("/");
								final int[] index = new int[] { Integer.parseInt(faceData[0]) - 1,
										Integer.parseInt(faceData[1]) - 1, Integer.parseInt(faceData[2]) - 1 };

								int indexPosition = -1;

								for (int j = 0; j < indices.size(); j++) {
									final int[] check = indices.get(j);
									if (check[0] == index[0] && check[1] == index[1] && check[2] == index[2]) {
										indexPosition = j;
										break;
									}
								}

								if (indexPosition == -1) {
									indices.add(index);
									indexOrder.add(indices.size() - 1); // index[0]
								} else {
									// indices.add(indices.get(indexPosition));
									indexOrder.add(indexPosition); // indices.get(indexPosition)[0]
								}
							}
						}
					} else if (data[0].equals("b")) {
						boneIndices.add(Byte.parseByte(data[1]));
					}
				}

				line = reader.readLine();
			}

			reader.close();

			final float[] vertexArray = new float[indices.size() * 3];
			final float[] uvArray = new float[indices.size() * 2];
			final float[] normalArray = new float[indices.size() * 3];
			final byte[] boneArray = new byte[boneIndices.size()];
			final int[] indexArray = new int[indexOrder.size()];

			for (int i = 0; i < indexArray.length; i++) {
				indexArray[i] = indexOrder.get(i);// indices.get(indexOrder.get(i))[0];
			}

			for (int i = 0; i < vertexArray.length / 3; i++) {
				final float[] vertex = vertices.get(indices.get(i)[0]);
				vertexArray[i * 3 + 0] = vertex[0];
				vertexArray[i * 3 + 1] = vertex[1];
				vertexArray[i * 3 + 2] = vertex[2];

				final float[] uv = uvs.get(indices.get(i)[1]);
				uvArray[i * 2 + 0] = uv[0];
				uvArray[i * 2 + 1] = 1 - uv[1];

				final float[] normal = normals.get(indices.get(i)[2]);
				normalArray[i * 3 + 0] = normal[0];
				normalArray[i * 3 + 1] = normal[1];
				normalArray[i * 3 + 2] = normal[2];

				if (boneArray.length > 0) {
					boneArray[i] = boneIndices.get(i);
				}
			}

			final Mesh mesh = Mesh.create();
			mesh.bind();
			mesh.createIndexBuffer(indexArray);
			mesh.createAttribute(0, vertexArray, 3);
			mesh.createAttribute(1, uvArray, 2);
			mesh.createAttribute(2, normalArray, 3);
			mesh.setBounds(max, min);

			if (boneArray.length > 0) {
				mesh.createAttribute(3, boneArray, 1);
			}
			mesh.unbind();

			return mesh;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
