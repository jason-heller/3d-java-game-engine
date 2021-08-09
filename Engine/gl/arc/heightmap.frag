#version 150

in vec4 pass_textureCoords;
in float pass_blends;
in vec3 pass_worldPos;
in vec4[4] shadowCoords;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D lightmap;
uniform vec3 lightDirection;

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
   
    //float bias = max(0.0001 * (1.0 - dot(-pass_normals, lightDir[i])), 0.00001);  
    float shadow = currentDepth > closestDepth ? 1.0 : 0.0;

    return shadow;
} 

void main(void){
	vec4 color, light;
	vec4 sample1 = texture(sampler1, pass_textureCoords.xy);
	vec4 sample2 = texture(sampler2, pass_textureCoords.xy);
	color = mix(sample1, sample2, pass_blends);

	light = texture(lightmap, pass_textureCoords.zw);
	
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

	color.a = 1.0;
	out_color = ((color * (light + max(intensity, lightMin))) * lightScale);
	out_brightness = vec4(0.0);
}
