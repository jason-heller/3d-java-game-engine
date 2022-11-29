#version 150

in vec3 vertex;

out vec3 faceNormal;
out vec4 vertClipSpace;

uniform mat4 viewModel;
uniform mat4 projection;

void main () {

	vec4 vert = vec4(vertex, 1.0);   
	
	vec4 vertViewSpace = viewModel * vert;  
	vertClipSpace = projection * vertViewSpace;
	
	gl_Position = vertClipSpace;    
}