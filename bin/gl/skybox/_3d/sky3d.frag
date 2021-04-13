#version 150

in vec4 pass_textureCoords;
in vec3 pass_normals;

uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform vec3 lightDirection;

out vec4 out_color;
const vec2 lightBias = vec2(0.3, 0.7);

void main(void){
	vec4 color, light;
	color = texture(sampler, pass_textureCoords.xy);
	light = texture(lightmap, pass_textureCoords.zw);
	
	if (color.rgb == vec3(1.0,0.0,1.0) || color.a == 0.0) {
		discard;
	}
	
	vec4 ambient = vec4(0.25,0.25,0.25,0.0);
	color.a = 1.0;
	out_color = color * (light + ambient);
	
}
