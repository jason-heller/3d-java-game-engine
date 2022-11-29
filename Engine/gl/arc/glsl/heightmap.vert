#version 150

const int MAX_LIGHTS = 4;

in vec3 in_position;
in vec4 in_textureCoords;
in float in_blends;

out float pass_blends;
out vec4 pass_textureCoords;
out vec3 pass_worldPos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec4 clipPlane;

uniform mat4 lightPos;
uniform mat4[MAX_LIGHTS] lightSpaceMatrix;

out mat4 pass_lightPos;
out mat4 shadowCoords;

void main(void){

	vec4 worldPos = vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionMatrix * viewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_blends = in_blends;
	pass_worldPos = worldPos.xyz;
	
	for(int i = 0; i < MAX_LIGHTS; i++) {
		shadowCoords[i] = lightSpaceMatrix[i] * vec4(worldPos.xyz, 1.0);
		pass_lightPos[i] = vec4((lightPos[i].xyz) - pass_worldPos, lightPos[i].w);
	}
}
