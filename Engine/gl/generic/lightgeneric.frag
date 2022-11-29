#version 150

in vec4 pass_textureCoords;
in vec3 pass_normals;
in vec3 pass_worldPos;
in vec4[4] shadowCoords;
in vec3 lightColor;

uniform vec4 color;
uniform sampler2D diffuse;

out vec4 out_color;
out vec4 out_brightness;

const float lightMin = 0.1;
const float lightScale = 0.9;

uniform sampler2D[4] shadowMap;
uniform vec3[4] lightPos;
uniform vec3[4] lightDir;
uniform vec2[4] cutoff;
uniform float[4] strength;

float ShadowCalculation(vec4 projLightSpace, int i) {
    vec3 projCoords = projLightSpace.xyz / projLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    float closestDepth = texture(shadowMap[i], projCoords.xy).r; 
    float currentDepth = projCoords.z;
   
	float bias = max(0.005 * (1.0 - dot(pass_normals, lightDir[i])), 0.01);  
    float shadow = currentDepth < closestDepth + bias ? 0.0 : 1.0;

    return shadow;
} 

void main(void){
	vec4 albedo, light;
	albedo = texture(diffuse, pass_textureCoords.xy);
	
	if (albedo.a < 0.5)
		discard;
		
	// Spotlight
	float intensity = 0.0;
	float shadow = 0.0;
	
	for(int i = 0; i < 4; i++) {
		if (strength[i] == 0.0) {
			break;
		}
		
		shadow = ShadowCalculation(shadowCoords[i], i); 
		
		if (shadow == 1.0) {
			continue;
		}
		
		vec3 toLight = lightPos[i] - pass_worldPos;
		vec3 lightProjVec = normalize(toLight);
		
		float dist = 1.0 + (length(toLight) / strength[i]);
		float theta = dot(lightProjVec, normalize(-lightDir[i]));
		float epsilon = cutoff[i].x - cutoff[i].y;
		
		intensity += clamp((theta - cutoff[i].y) / epsilon, 0.0, 1.0) / dist;
	}

	albedo.a = 1.0;
	float lightScale = max(intensity, lightMin);
	out_color = (albedo * vec4(lightColor + vec3(lightScale), 1.0));
	out_color = mix(out_color, vec4(color.xyz, 1.0), color.w);
	out_brightness = vec4(0.0);
}
