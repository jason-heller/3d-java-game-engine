package gl.line;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import geom.Polygon;
import gl.Camera;
import gl.Render;
import gl.res.Model;

public class LineRender {
	private static final Vector3f DEFAULT_LINE_COLOR = new Vector3f(1, 0, 1);
	private static LineShader shader;
	private static List<Vector3f> points;
	private static List<Vector3f> colors;
	private static Model line;
	private static int vbo;
	
	private static int pointer = 0;
	
	private static final int MAX_LINES = 12000;
	private static final int INSTANCE_DATA_LENGTH = 9;
	private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_LINES * INSTANCE_DATA_LENGTH);
	
	public static void init() {
		shader = new LineShader();
		points = new ArrayList<Vector3f>();
		colors = new ArrayList<Vector3f>();
		GL11.glLineWidth(3f);
		makeLineVao();
		
		vbo = createEmptyVbo(INSTANCE_DATA_LENGTH * MAX_LINES);
		addInstancedAttrib(line.id, vbo, 1, 3, INSTANCE_DATA_LENGTH, 0);
		addInstancedAttrib(line.id, vbo, 2, 3, INSTANCE_DATA_LENGTH, 3);
		addInstancedAttrib(line.id, vbo, 3, 3, INSTANCE_DATA_LENGTH, 6);
	}
	
	public static void cleanUp() {
		points.clear();
		colors.clear();
		line.cleanUp();
		GL15.glDeleteBuffers(vbo);
		shader.cleanUp();
	}

	private static int createEmptyVbo(int numFloats) {
		int vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 4 * numFloats, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vbo;
	}

	private static void addInstancedAttrib(int vao, int vbo, int attrib, int dataSize, int stride, int offset) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL30.glBindVertexArray(vao);

		GL20.glVertexAttribPointer(attrib, dataSize, GL11.GL_FLOAT, false, stride * 4, offset * 4);
		GL33.glVertexAttribDivisor(attrib, 1);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	
	private static void updateVbo(float[] data, FloatBuffer buffer) {

		buffer.clear();
		buffer.put(data);
		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	private static void makeLineVao() {
		line = Model.create();
		line.bind();
		line.createAttribute(0, new float[] { 0, 0, 0, 0, 0, 0 }, 3); // -0.5f,-0.5f, 0.5f,-0.5f, -0.5f,0.5f, 0.5f,0.5f
		line.unbind();
	}
	
	public static void render(Camera camera) {
		
		if (points.size() == 0)
			return;

		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		line.bind(0, 1, 2, 3);
		
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		
		pointer = 0;
		float[] vboData = new float[colors.size() * INSTANCE_DATA_LENGTH];
		storeVboData(vboData);
		updateVbo(vboData, buffer);
		
		GL31.glDrawArraysInstanced(GL11.GL_LINE_STRIP, 0, 2, colors.size());
		Render.drawCalls++;
		
		line.unbind(0, 1, 2, 3);
		shader.stop();
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		colors.clear();
		points.clear();
	}
	
	private static void storeVboData(float[] vboData) {
		for(int i = 0; i < colors.size(); i ++) {
			vboData[pointer++] = points.get(i * 2).x;
			vboData[pointer++] = points.get(i * 2).y;
			vboData[pointer++] = points.get(i * 2).z;
			vboData[pointer++] = points.get(i * 2 + 1).x;
			vboData[pointer++] = points.get(i * 2 + 1).y;
			vboData[pointer++] = points.get(i * 2 + 1).z;
			vboData[pointer++] = colors.get(i).x;
			vboData[pointer++] = colors.get(i).y;
			vboData[pointer++] = colors.get(i).z;
		}
	}

	/*public static void render(Camera cam) {
		if (points.size() == 0) return;
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		line.bind();
		shader.projectionViewMatrix.loadMatrix(cam.getProjectionViewMatrix());
		int j = 0;
		for (int i = 0; i < points.size(); i += 2) {
			shader.color.loadVec3(colors.get(j++));
			shader.point1.loadVec3(points.get(i));
			shader.point2.loadVec3(points.get(i + 1));
			GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
		}
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		line.unbind();
		shader.stop();
	}*/
	
	public static void drawPoint(Vector3f point) {
		drawLine(point, Vector3f.add(point, Vector3f.Y_AXIS));
	}
	
	public static void drawLine(Vector3f p1, Vector3f p2) {
		drawLine(p1, p2, DEFAULT_LINE_COLOR);
	}
	
	public static void drawLine(Vector3f p1, Vector3f p2, Vector3f color) {
		points.add(p1);
		points.add(p2);
		colors.add(color);
	}

	public static void drawBox(Vector3f center, Vector3f bounds, Vector3f color) {
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,1,1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,1,1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,1,1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,1,-1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,1,-1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,1,-1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,1,-1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,1,1))), color);
		
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,-1,1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,-1,1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,-1,1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,-1,-1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,-1,-1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,-1,-1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,-1,-1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,-1,1))), color);
		
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,-1,1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,1,1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,-1,-1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(1,1,-1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,-1,-1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,1,-1))), color);
		LineRender.drawLine(Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,-1,1))),
				Vector3f.add(center, Vector3f.mul(bounds, new Vector3f(-1,1,1))), color);
	}

	public static void drawTriangle(Polygon tri, Vector3f color) {
		LineRender.drawLine(tri.p1, tri.p2, color);
		LineRender.drawLine(tri.p2, tri.p3, color);
		LineRender.drawLine(tri.p3, tri.p1, color);
	}
}
