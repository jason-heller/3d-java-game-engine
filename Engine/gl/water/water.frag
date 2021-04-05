#version 150

in vec2 pass_textureCoords;
in vec3 cameraVec;
in vec4 clipSpace;

uniform sampler2D reflection;
uniform sampler2D refraction;
uniform sampler2D dudv;
uniform sampler2D depth;

uniform float timer;

out vec4 out_color;

const float near = 0.1;
const float far = 8000.0;

void main(void){
	vec2 normalizedDeviceSpace = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
	
	float d = texture(depth, normalizedDeviceSpace).r;
	float dCoord = 2.0 * near * far / (far + near - (2.0 * d - 1.0) * (far - near));
	d = gl_FragCoord.z;
	float wCoord = 2.0 * near * far / (far + near - (2.0 * d - 1.0) * (far - near));
	float waterDepth = dCoord - wCoord;

	float scroll = timer / 50.0;
	vec2 offset = (texture(dudv, vec2(pass_textureCoords.x + scroll, pass_textureCoords.y + scroll)).rg*2.0 - 1.0) * .025;
	offset *= clamp(waterDepth / 40.0, 0.0, 1.0);
	
	normalizedDeviceSpace += offset;
	normalizedDeviceSpace = clamp(normalizedDeviceSpace, 0.001, 0.999);
	
	vec4 reflectionColor = texture(reflection, vec2(normalizedDeviceSpace.x, 1.0 - normalizedDeviceSpace.y));	
	vec4 refractionColor = texture(refraction, normalizedDeviceSpace);	
	
	vec3 cameraNormal = normalize(cameraVec);
	float refracFactor = dot(cameraNormal, vec3(0,1,0));
	
	out_color = mix(reflectionColor, refractionColor, refracFactor);
	out_color.a = clamp(waterDepth / 20.0, 0.0, 1.0);
}
