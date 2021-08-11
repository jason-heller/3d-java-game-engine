#version 150

in vec2 pass_uvs;
in vec3 pass_normals;
in vec3 lightColor;

uniform sampler2D diffuse;
uniform vec4 color;

out vec4 out_color;

const float ambientFactor = .9;
const float baseAmbient = 1.0 - ambientFactor;

void main(void) {
	vec4 diffuse = texture(diffuse, pass_uvs);
	if (diffuse.a < 0.1)
		discard;
	
	out_color = diffuse * color * vec4((lightColor.xyz * ambientFactor) + vec3(baseAmbient), 1.0);
}
