package gl.map.architecture.render;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec4;

public class ArcShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/map/architecture/render/arc.vert";
	private static final String FRAGMENT_SHADER = "gl/map/architecture/render/arc.frag";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");
	protected UniformSampler lightmap = new UniformSampler("lightmap");
	public UniformVec3 camPos = new UniformVec3("camPos");
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");

	public ArcShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(projectionMatrix, camPos, viewMatrix, modelMatrix, lightmap, sampler, clipPlane);
		super.bindFragOutput(0, "out_color");
		super.bindFragOutput(1, "out_brightness");
	}
}
