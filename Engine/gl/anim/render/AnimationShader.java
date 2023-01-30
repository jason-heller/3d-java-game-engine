package gl.anim.render;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMat4;
import shader.UniformMat4Array;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec3Array;

public class AnimationShader extends ShaderProgram {

	private static final int MAX_JOINTS = 50;

	protected static final String VERTEX_SHADER = "gl/anim/render/anim.vert";
	protected static final String FRAGMENT_SHADER = "gl/anim/render/anim.frag";

	protected UniformMat4 projectionViewMatrix = new UniformMat4("projectionViewMatrix");
	protected UniformMat4 modelMatrix = new UniformMat4("modelMatrix");
	//protected UniformMat3 invTransRotMatrix = new UniformMat3("invTransRotMatrix");
	protected UniformVec3 lightDirection = new UniformVec3("lightDirection");
	protected UniformVec3 cameraPos = new UniformVec3("cameraPos");

	protected UniformMat4Array jointTransforms = new UniformMat4Array("jointTransforms", MAX_JOINTS);

	protected UniformSampler diffuse = new UniformSampler("diffuse");
	protected UniformSampler specular = new UniformSampler("specular");
	protected UniformFloat specularity = new UniformFloat("specularity");
	
	public UniformVec3Array lights = new UniformVec3Array("lights", 6);

	public AnimationShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normal", "in_jointIndices",
				"in_weights");
		super.storeAllUniformLocations(projectionViewMatrix, modelMatrix, /*invTransRotMatrix,*/ diffuse, specular, lightDirection,
				jointTransforms, specularity, cameraPos, lights);
		super.bindFragOutput(0, "out_color");
		super.bindFragOutput(1, "out_brightness");
	}
}
