package shader;

import org.joml.Matrix4f;

public class UniformMat4Array extends Uniform {

	private final UniformMat4[] matrixUniforms;

	public UniformMat4Array(String name, int size) {
		super(name);
		matrixUniforms = new UniformMat4[size];
		for (int i = 0; i < size; i++) {
			matrixUniforms[i] = new UniformMat4(name + "[" + i + "]");
		}
	}

	public void loadMatrixArray(Matrix4f[] matrices) {
		for (int i = 0; i < matrices.length; i++) {
			matrixUniforms[i].loadMatrix(matrices[i]);
		}
	}

	public void loadMatrix(int index, Matrix4f matrix) {
		matrixUniforms[index].loadMatrix(matrix);
	}
	
	@Override
	protected void storeUniformLocation(int programID) {
		for (final UniformMat4 matrixUniform : matrixUniforms) {
			matrixUniform.storeUniformLocation(programID);
		}
	}

}
