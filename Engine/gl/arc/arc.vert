#version 150

in vec3 in_position;
in vec4 in_textureCoords;
in vec3 in_normals;

out vec4 pass_textureCoords;
out vec3 pass_normals;
out vec3 pass_worldPos;
out vec4[4] shadowCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4[4] lightSpaceMatrix;
uniform vec4 clipPlane;

void main(void){

	vec4 worldPos = modelMatrix * vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionMatrix * viewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
	pass_worldPos = worldPos.xyz;
	
	for(int i = 0; i < 4; i++) {
		shadowCoords[i] = lightSpaceMatrix[i] * vec4(worldPos.xyz, 1.0);
	}
}
