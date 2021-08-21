#version 150

in vec3 in_position;
in vec4 in_textureCoords;
in vec3 in_normals;
in vec3 in_tangents;

out vec4 pass_textureCoords;
out vec3 pass_normals;
out vec3 pass_worldPos;
out vec4[4] shadowCoords;

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;
uniform mat4[4] lightSpaceMatrix;
uniform vec4 clipPlane;

uniform vec3 lights[6];

out vec3 lightColor;

const vec3 lightNormals[6] = vec3[6](vec3(1.0,0.0,0.0), vec3(-1.0,0.0,0.0), vec3(0.0,1.0,0.0), vec3(0.0,-1.0,0.0), vec3(0.0,0.0,1.0), vec3(0.0,0.0,-1.0));

void main(void){

	vec4 worldPos = modelMatrix * vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionViewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
	
	pass_worldPos = worldPos.xyz;
	
	for(int i = 0; i < 4; i++) {
		shadowCoords[i] = lightSpaceMatrix[i] * vec4(worldPos.xyz, 1.0);
	}
	
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
