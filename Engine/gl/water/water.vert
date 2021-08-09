#version 150

in vec3 in_position;
in vec2 in_textureCoords;

out vec2 pass_textureCoords;
out vec3 cameraVec;
out vec4 clipSpace;

uniform mat4 projectionViewMatrix;
uniform vec3 offset;
uniform vec3 cameraPos;
uniform vec2 scales;

void main(void){

	float dx = (in_position.x * scales.x) + offset.x;
	float dy = in_position.y + offset.y;
	float dz = (in_position.z * scales.y) + offset.z;
	vec4 worldPos = vec4(dx, dy, dz, 1.0);
	
	clipSpace = projectionViewMatrix * worldPos;
	gl_Position = clipSpace;
	
	pass_textureCoords = in_textureCoords;
	cameraVec = cameraPos - worldPos.xyz;
}
