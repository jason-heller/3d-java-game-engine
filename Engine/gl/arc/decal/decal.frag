#version 150

in vec4 vertClipSpace;

uniform sampler2D depthSamples;
uniform sampler2D decal;

uniform mat4 invModelView;
uniform mat4 invProj;

out vec4 outColor;

void main() {
	vec2 normDevCoord = vertClipSpace.xy / vertClipSpace.w;
	
	vec2 texCoord = normDevCoord * 0.5 + 0.5; 
	
	float depth = (texture(depthSamples, texCoord).r) * 2.0 - 1.0;
	
	vec3 ndcSample = vec3(normDevCoord.xy, depth);
	   
	vec4 viewRay = invProj * vec4(ndcSample, 1.0);
	vec3 viewPosition = viewRay.xyz / viewRay.w;
	
	// view space -> object space 
	vec3 objectPosition = (invModelView * vec4(viewPosition, 1.0)).xyz;
	
	if (abs(objectPosition.x) > 0.5
	||  abs(objectPosition.y) > 0.5   
	||  abs(objectPosition.z) > 0.5) discard;    
	
	vec2 decalCoords = objectPosition.xz + 0.5;
	outColor = texture(decal, decalCoords);
}