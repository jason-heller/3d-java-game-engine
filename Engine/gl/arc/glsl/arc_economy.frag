#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;

in vec4 pass_textureCoords;
in vec3 pass_worldPos;

uniform sampler2D sampler;
uniform sampler2D lightmap;

out vec4 out_color;

void main(void){
	vec4 color, light;
	vec3 normalMap;
	color = texture(sampler, pass_textureCoords.xy);

	light = texture(lightmap, pass_textureCoords.zw);
	
	color.a = 1.0;
	out_color = (color * (light + lightMin)) * lightScale;
}
