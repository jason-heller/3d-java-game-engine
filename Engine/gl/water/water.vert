#version 150

in vec3 in_position;
in vec2 in_textureCoords;

out vec2 pass_textureCoords;
out vec4 clipSpace;

uniform mat4 projectionViewMatrix;
uniform vec3 offset;
uniform vec2 scales;

void main(void){

	float dx = (in_position.x * scales.x) + offset.x;
	float dy = in_position.y + offset.y;
	float dz = (in_position.z * scales.y) + offset.z;
	
	clipSpace = projectionViewMatrix * vec4(dx, dy, dz, 1.0);
	gl_Position = clipSpace;
	
	pass_textureCoords = in_textureCoords;
}
