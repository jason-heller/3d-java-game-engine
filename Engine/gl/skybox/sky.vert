#version 150

in vec3 in_position;
out vec3 pass_uvs;

uniform vec3 lightDir;
uniform mat4 projectionViewMatrix;

void main(void){
	gl_Position = projectionViewMatrix * vec4(in_position, 1.0);
	pass_uvs = in_position;

}

