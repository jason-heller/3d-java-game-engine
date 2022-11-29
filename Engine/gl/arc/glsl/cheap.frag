#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;

in vec4 pass_textureCoords;

uniform sampler2D sampler;
uniform sampler2D lightmap;

out vec4 outputColor;

void main(void){
	vec4 color = texture(sampler, pass_textureCoords.xy);
	vec4 light = texture(lightmap, pass_textureCoords.zw);
	
	color.a = 1.0;
	outputColor = color * (light + lightMin) * lightScale;
}
