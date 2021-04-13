#version 330

layout(location = 1) in vec3 point1;
layout(location = 2) in vec3 point2;
layout(location = 3) in vec3 color;

uniform mat4 projectionViewMatrix;

out vec3 col;

void main(void){

	col = color;
	
	if (gl_VertexID == 0)
		gl_Position = projectionViewMatrix * vec4(point1, 1.0);
		
	if (gl_VertexID == 1)
		gl_Position = projectionViewMatrix * vec4(point2, 1.0);

	if (gl_VertexID == 2)
			gl_Position = projectionViewMatrix * vec4(point1 + vec3(0.0), 1.0);
			
	if (gl_VertexID == 3)
		gl_Position = projectionViewMatrix * vec4(point2 + vec3(0.0), 1.0);

}
