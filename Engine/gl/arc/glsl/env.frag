#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;
const float shineDampen = 1.0;
const int MAX_LIGHTS = 4;

in vec4 pass_textureCoords;
in vec3 pass_normals;
in vec3 pass_worldPos;

in vec3 pass_vertices;

uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform sampler2D specMap;
uniform samplerCube envMap;

uniform sampler2D[MAX_LIGHTS] shadowMap;

in mat4 pass_lightPos;
uniform mat4 lightDir;
in mat4 shadowCoords;

uniform vec3 cubemapMax;
uniform vec3 cubemapMin;

uniform vec3 cameraPos;

uniform vec4 lightInfo;
uniform mat3 lightStyles;

out vec4 outputColor;

float ShadowCalculation(vec4 projLightSpace, int i) {
    vec3 projCoords = projLightSpace.xyz / projLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    float closestDepth = texture(shadowMap[i], projCoords.xy).r; 
    float currentDepth = projCoords.z;
   
	float bias = max(0.005 * (1.0 - dot(pass_normals, lightDir[i].xyz)), 0.01);  
    float shadow = currentDepth < closestDepth + bias ? 0.0 : 1.0;

    return shadow;
} 

void main(void){
	vec4 color, light;
	vec3 normalMap;
	color = texture(sampler, pass_textureCoords.xy);

	if (color.a < 0.5)
		discard;
	
	light = texture(lightmap, pass_textureCoords.zw);
	for(int i = 0; i < 3; i++) {
		light += texture(lightmap, pass_textureCoords.zw + lightStyles[i].xy) * lightStyles[i].z;
	}
	
	float intensity = 0.0;
	float shadow = 0.0;
	
	for(int i = 0; i < MAX_LIGHTS; i++) {
		if (lightInfo[i] == 0.0)
			break;
		
		shadow = ShadowCalculation(shadowCoords[i], i); 
		
		if (shadow == 1.0)
			continue;
		
		vec3 lightProjVec = normalize(pass_lightPos[i].xyz);
		vec3 lightDirComp = -lightDir[i].xyz;
		
		float dist = 1.0 + (length(pass_lightPos[i]) / lightInfo[i]);
		float theta = dot(lightProjVec, lightDirComp);

		float epsilon = (pass_lightPos[i].w - lightDir[i].w);
		
		intensity += (clamp(((theta - lightDir[i].w) / epsilon), 0.0, 1.0) / dist);
	}
	
	vec3 camVector = pass_vertices - cameraPos;
	vec3 specularCoords = reflect(camVector, pass_normals);
	
	// Parallax correction
	vec3 intersectA = (cubemapMax - pass_vertices) / specularCoords;
	vec3 intersectB = (cubemapMin - pass_vertices) / specularCoords;
	
	// Get the furthest of these intersections along the ray
	vec3 furthestPlane = max(intersectA, intersectB);
	
	// Find the closest far intersection
	float distance = min(min(furthestPlane.x, furthestPlane.y), furthestPlane.z);
	
	// Get the intersection position
	vec3 intersectPos = (specularCoords * distance) + pass_vertices;
	vec3 cubemapPos = 0.5 * (cubemapMax + cubemapMin);
	
	specularCoords = normalize(intersectPos - cubemapPos);
	vec4 specularity = texture(envMap, specularCoords);
	vec4 specularFactor = texture(specMap, pass_textureCoords.xy);
	
	color.a = 1.0;
	outputColor = (color * (light + max(intensity, lightMin))) * lightScale;
	outputColor = mix(outputColor, specularity, specularFactor);
}
