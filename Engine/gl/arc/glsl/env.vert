#version 150

const int MAX_LIGHTS = 4;

in vec3 in_position;
in vec4 in_textureCoords;
in vec3 in_normals;
in vec3 in_tangents;

out vec4 pass_textureCoords;
out vec3 pass_normals;
out vec3 pass_worldPos;

uniform mat4 projectionViewMatrix;
uniform vec4 clipPlane;

uniform mat4 lightPos;
uniform mat4 lightDir;
uniform mat4[MAX_LIGHTS] lightSpaceMatrix;

out mat4 pass_lightPos;
out mat4 shadowCoords;

out vec3 pass_vertices;

void main(void){

	vec4 worldPos = vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionViewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
	pass_worldPos = worldPos.xyz;
	
	for(int i = 0; i < MAX_LIGHTS; i++) {
		shadowCoords[i] = lightSpaceMatrix[i] * vec4(in_position, 1.0);
		pass_lightPos[i] = vec4((lightPos[i].xyz) - in_position, lightPos[i].w);\
	}
	
	pass_vertices = in_position;
}
