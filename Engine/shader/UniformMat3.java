package shader;

import java.nio.FloatBuffer;

import org.joml.Matrix3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

public class UniformMat3 extends Uniform {

	private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(9);

	public UniformMat3(String name) {
		super(name);
	}

	public void loadMatrix(Matrix3f matrix) {
		matrix.get(0, matrixBuffer);
		GL20.glUniformMatrix3(super.getLocation(), false, matrixBuffer);
	}
	
	public void loadMatrix(float[] matrix) {
		matrixBuffer.put(matrix);
		matrixBuffer.flip();
		GL20.glUniformMatrix3(super.getLocation(), false, matrixBuffer);
	}

	public void loadMatrix(FloatBuffer matrixBuffer) {
		GL20.glUniformMatrix3(super.getLocation(), false, matrixBuffer);
	}
}
