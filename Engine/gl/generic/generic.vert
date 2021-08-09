#version 150

in vec3 in_vertices;
in vec2 in_uvs;
in vec3 in_normals;

out vec2 pass_uvs;
out vec3 pass_normals;
out vec3 lightColor;

uniform vec3 lights[6];

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;

const vec3 lightNormals[6] = vec3[6](vec3(1.0,0.0,0.0), vec3(-1.0,0.0,0.0), vec3(0.0,1.0,0.0), vec3(0.0,-1.0,0.0), vec3(0.0,0.0,1.0), vec3(0.0,0.0,-1.0));

void main(void) {

	vec4 worldPos = modelMatrix * vec4(in_vertices, 1.0);
	gl_Position = projectionViewMatrix * worldPos;

	pass_uvs = in_uvs;
	pass_normals = (vec4(in_normals, 1.0) * modelMatrix).xyz;
	
	lightColor = vec3(0.0);
	
	float totalWeight = 0.0;
	for(int i = 0; i < 6; ++i) {
		float weight = dot(pass_normals, lightNormals[i]);
		if (weight <= 0.0)
			continue;
		lightColor += lights[i] * weight;
		totalWeight += weight;
	}
	lightColor /= totalWeight;
	
}
