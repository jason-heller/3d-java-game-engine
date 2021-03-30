#version 150

in vec2 pass_uvs;
in vec3 pass_normals;

uniform sampler2D diffuse;
uniform vec3 lightDirection;

out vec4 out_color;

const float ambientLight = 0.5;
const float lightScale = 0.3;

void main(void) {
	vec4 diffuse = texture(diffuse, pass_uvs);

	float light = dot(vec3(lightDirection.x, 0, lightDirection.z), pass_normals);
	light = (light * lightScale) + ambientLight;
	
	out_color = diffuse * light;
}
