package gl.arc;

import org.joml.Vector3f;

import geom.AxisAlignedBBox;
import map.architecture.components.ArcClip;
import shader.UniformSampler;
import shader.UniformVec3;
import util.Vectors;

public class ArcShaderEnvMap extends ArcShaderBase {

	protected static final String VERTEX_SHADER = "gl/arc/glsl/env.vert";
	protected static final String FRAGMENT_SHADER = "gl/arc/glsl/env.frag";

	public UniformSampler specMap = new UniformSampler("specMap");
	protected UniformSampler envMap = new UniformSampler("envMap");
	public UniformVec3 cameraPos = new UniformVec3("cameraPos");
	
	private UniformVec3 cubemapMax = new UniformVec3("cubemapMax");
	private UniformVec3 cubemapMin = new UniformVec3("cubemapMin");

	public ArcShaderEnvMap() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		addUniforms(specMap, envMap, cameraPos, cubemapMax, cubemapMin);
	}

	public void loadBoundingBox(ArcClip clip) {
		AxisAlignedBBox box = clip.bbox;
		
		Vector3f max = Vectors.add(box.getCenter(), box.getBounds());
		Vector3f min = Vectors.sub(box.getCenter(), box.getBounds());

		cubemapMax.loadVec3(max);
		cubemapMin.loadVec3(min);
	}
}
