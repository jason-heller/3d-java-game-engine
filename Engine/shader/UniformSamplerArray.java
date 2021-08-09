package shader;

public class UniformSamplerArray extends Uniform {

	private final UniformSampler[] uniforms;

	public UniformSamplerArray(String name, int size) {
		super(name);
		uniforms = new UniformSampler[size];
		for (int i = 0; i < size; i++) {
			uniforms[i] = new UniformSampler(name + "[" + i + "]");
		}
	}
	
	public void loadTexUnit(int texUnit) {
		int i = 0;
		for (final UniformSampler matrixUniform : uniforms) {
			matrixUniform.loadTexUnit(texUnit + i);
			i++;
		}
	}
	
	@Override
	protected void storeUniformLocation(int programID) {
		for (final UniformSampler matrixUniform : uniforms) {
			matrixUniform.storeUniformLocation(programID);
		}
	}

}
