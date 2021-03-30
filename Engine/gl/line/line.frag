#version 150

in vec3 col;
out vec4 out_color;

void main(void){
	out_color = vec4(col, 1.0);
}
