#version 150

in vec3 in_position;
in vec4 in_textureCoords;
in vec3 in_normals;
in vec3 in_tangents;

out vec4 pass_textureCoords;

out vec3 pass_worldPos;

uniform mat4 projectionViewMatrix;
uniform vec4 clipPlane;

void main(void){

	vec4 worldPos = vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);
	gl_Position = projectionViewMatrix * worldPos;
	
	pass_textureCoords = in_textureCoords;
	pass_worldPos = worldPos.xyz;
}
