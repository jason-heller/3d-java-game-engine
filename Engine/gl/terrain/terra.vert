#version 150

in vec3 in_vertices;
in vec2 in_uvs;
in vec4 in_heightsNormals;

out vec2 pass_uvs;
out vec3 pass_normals;

uniform vec2 offset;
uniform mat4 projectionViewMatrix;

void main(void) {

	vec4 worldPos = vec4(in_vertices.x + offset.x, in_vertices.y + in_heightsNormals.x, in_vertices.z + offset.y, 1.0);
	gl_Position = projectionViewMatrix * worldPos;

	pass_uvs = in_uvs;
	pass_normals = vec3(in_heightsNormals.y, in_heightsNormals.z, in_heightsNormals.w);
}
