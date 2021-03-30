#version 150

in vec3 pass_uvs;
uniform samplerCube sampler;
out vec4 out_colour;
out vec4 out_brightness;
uniform vec3 viewDir;
in float lightAmt;
in float lightY;


void main(void){
	out_colour = texture(sampler, pass_uvs);
	out_brightness = vec4(0.0);
}
