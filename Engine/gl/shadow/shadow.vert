#version 150
in vec3 position;

uniform mat4 lightSpaceMatrix;
uniform mat4 modelMatrix;

void main() {
    gl_Position = lightSpaceMatrix * modelMatrix * vec4(position, 1.0);
} 