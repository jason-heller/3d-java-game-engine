package shader;

public class UniformFloatArray extends Uniform {

	private final UniformFloat[] uniforms;

	public UniformFloatArray(String name, int size) {
		super(name);
		uniforms = new UniformFloat[size];
		for (int i = 0; i < size; i++) {
			uniforms[i] = new UniformFloat(name + "[" + i + "]");
		}
	}

	public void loadFloat(int index, float f) {
		uniforms[index].loadFloat(f);
	}

	public void loadFloats(float[] floats) {
		for (int i = 0; i < floats.length; i++) {
			uniforms[i].loadFloat(floats[i]);
		}
	}

	@Override
	protected void storeUniformLocation(String name, int programID) {
		for (final UniformFloat matrixUniform : uniforms) {
			matrixUniform.storeUniformLocation(name, programID);
		}
	}
}
