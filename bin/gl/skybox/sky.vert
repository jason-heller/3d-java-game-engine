#version 150

in vec3 in_position;
out vec3 pass_uvs;
out float lightAmt;
out float lightY;

uniform vec3 lightDir;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){
	lightAmt = dot(-lightDir,normalize(in_position));
	lightY = lightDir.y;

	gl_Position = projectionMatrix * viewMatrix * vec4(in_position, 1.0);
	pass_uvs = in_position;

}

