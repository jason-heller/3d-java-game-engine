#version 150

in vec3 in_position;
in vec4 in_textureCoords;
in vec3 in_normals;
in vec3 in_tangents;

out vec4 pass_textureCoords;
out vec3 pass_normals;
out vec3 pass_worldPos;
out vec4[4] shadowCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4[4] lightSpaceMatrix;
uniform vec4 clipPlane;

uniform vec3[4] lightPos;
uniform vec3[4] lightDir;

out vec3[4] pass_lightPos;
out vec3[4] pass_lightDir;

void main(void){

	vec4 worldPos = vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionMatrix * viewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
	
	vec3 bitangent = cross(in_normals, in_tangents);
	mat3 tangentSpaceMatrix = mat3(
		in_tangents.x, bitangent.x, in_normals.x,
		in_tangents.y, bitangent.y, in_normals.y,
		in_tangents.z, bitangent.z, in_normals.z
	);
	
	pass_worldPos = tangentSpaceMatrix * worldPos.xyz;
	
	for(int i = 0; i < 4; i++) {
		shadowCoords[i] = lightSpaceMatrix[i] * vec4(worldPos.xyz, 1.0);
		pass_lightPos[i] = tangentSpaceMatrix * (lightPos[i] - worldPos.xyz);
		pass_lightDir[i] = tangentSpaceMatrix * lightDir[i];
	}
}
