#version 150

in vec3 in_position;
in vec4 in_textureCoords;
in vec3 in_normals;

out vec4 pass_textureCoords;
out vec3 pass_normals;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec4 clipPlane;
uniform vec3 camPos;

void main(void){

	vec4 worldPos = modelMatrix * vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionMatrix * viewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
}
