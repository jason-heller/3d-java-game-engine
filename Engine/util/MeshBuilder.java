package util;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;

import gl.res.Mesh;
import gl.res.Vbo;

class Attribute {
	public int id;
	private List<Float> data;
	public int stride;

	public Attribute() {
		data = new ArrayList<Float>();
	}

	public float[] getData() {
		float[] arr = new float[data.size()];
		int i = 0;
		for (Float f : data) {
			arr[i++] = f;
		}
		return arr;
	}

	public void add(float f) {
		data.add(f);
	}
}

public class MeshBuilder {

	private int indexRel = 0;
	private List<Attribute> attribs;
	private List<Integer> indices = new ArrayList<Integer>();

	private Attribute vertices, uvs, normals, colors;

	public MeshBuilder(boolean hasUvs, boolean hasNormals, boolean hasColors) {
		indices = new ArrayList<Integer>();
		attribs = new ArrayList<Attribute>();
		
		int index = 1;

		addAttrib(0, 3);
		if (hasUvs) {
			addAttrib(index, 2);
			uvs = attribs.get(index++);
		}
		if (hasNormals) {
			addAttrib(index, 3);
			normals = attribs.get(index++);
		}
		if (hasColors) {
			addAttrib(index, 4);
			colors = attribs.get(index++);
		}

		vertices = attribs.get(0);
	}

	public int addAttrib(int id, int stride) {
		Attribute attrib = new Attribute();
		attrib.id = id;
		attrib.stride = stride;
		attribs.add(attrib);

		return attribs.size() - 1;
	}

	public void add(int id, float... data) {
		Attribute attrib = attribs.get(id);
		for (float f : data) {
			attrib.add(f);
		}
	}

	public void addVertex(float x, float y, float z) {
		vertices.add(x);
		vertices.add(y);
		vertices.add(z);
	}

	public void addUv(float tx, float ty) {
		uvs.add(tx);
		uvs.add(ty);
	}

	public void addNormal(float x, float y, float z) {
		normals.add(x);
		normals.add(y);
		normals.add(z);
	}

	public void addNormal(Vector3f n) {
		addNormal(n.x, n.y, n.z);
	}

	public void addColor(float r, float g, float b, float a) {
		colors.add(r);
		colors.add(g);
		colors.add(b);
		colors.add(a);
	}
	
	public void addColor(float r, float g, float b) {
		addColor(r, g, b, 1f);
	}

	public void addColor(Vector3f n) {
		addColor(n.x, n.y, n.z, 1f);
	}

	public static Mesh buildQuad(float[] vertices) {
		final float[] normals = new float[vertices.length];
		final Vector3f normal = getNormal(vertices, 0);
		for (int i = 0; i < 4; i++) {
			normals[i * 3 + 0] = normal.x;
			normals[i * 3 + 1] = normal.y;
			normals[i * 3 + 2] = normal.x;
		}

		final Mesh model = Mesh.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, new float[] { 0, 0, 1, 0, 0, 1, 1, 1 }, 2);
		model.createAttribute(2, normals, 3);
		model.createIndexBuffer(new int[] { 0, 1, 3, 3, 1, 2 });
		model.unbind();

		return model;
	}

	public Mesh createModel() {
		final Mesh model = Mesh.create();
		model.bind();
		for (Attribute attrib : attribs) {
			model.createAttribute(attrib.id, attrib.getData(), attrib.stride);
		}

		model.createIndexBuffer(getIndexArray());
		model.unbind();

		return model;
	}

	private int[] getIndexArray() {
		int[] inds = new int[indices.size()];
		for (int i = 0; i < inds.length; i++) {
			inds[i] = indices.get(i);
		}

		return inds;
	}

	public Mesh finish() {
		indexRel = 0;
		return createModel();
	}

	public Vbo[] asVbos() {
		indexRel = 0;
		final Vbo[] vbos = new Vbo[attribs.size() + 1];
		final int indexVbo = attribs.size();

		for (int i = 0; i < attribs.size(); i++) {
			vbos[i] = Vbo.create(GL15.GL_ARRAY_BUFFER);
			vbos[i].bind();
			vbos[i].storeData(attribs.get(i).getData());
			vbos[i].unbind();
		}

		vbos[indexVbo] = Vbo.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
		vbos[indexVbo].bind();
		vbos[indexVbo].storeData(getIndexArray());
		vbos[indexVbo].unbind();

		return vbos;
	}

	public static int[] genIndexPattern(int vertexStripSize) {
		int pointer = 0;
		final int[] indices = new int[6 * (vertexStripSize - 1) * (vertexStripSize - 1)];
		for (int gz = 0; gz < vertexStripSize - 1; gz++) {
			for (int gx = 0; gx < vertexStripSize - 1; gx++) {
				final int topLeft = gz * vertexStripSize + gx;
				final int topRight = topLeft + 1;
				final int bottomLeft = (gz + 1) * vertexStripSize + gx;
				final int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}

		return indices;
	}

	private static Vector3f getNormal(float[] v, int i) {
		final Vector3f p1 = new Vector3f(v[i - 9], v[i - 8], v[i - 7]);
		final Vector3f p2 = new Vector3f(v[i - 6], v[i - 5], v[i - 4]);
		final Vector3f p3 = new Vector3f(v[i - 3], v[i - 2], v[i - 1]);

		return Vectors.cross(Vectors.sub(p2, p1), Vectors.sub(p3, p1)).normalize();
	}

	public void addIndices(int... inds) {
		for (final int i : inds) {
			indices.add(i);
		}
	}

	public void addRelativeIndices(int jump, int... inds) {
		for (final int i : inds) {
			indices.add(indexRel + i);
		}

		indexRel += jump;
	}
}
