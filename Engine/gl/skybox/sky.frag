#version 150

in vec3 pass_uvs;
uniform samplerCube sampler;
out vec4 out_colour;
uniform vec3 viewDir;


void main(void){
	out_colour = texture(sampler, pass_uvs);
}
