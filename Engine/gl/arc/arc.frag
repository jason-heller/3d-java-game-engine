#version 150

in vec4 pass_textureCoords;
in vec3 pass_normals;
in vec3 pass_worldPos;
in vec4[4] shadowCoords;

uniform int hasBumpMap;

uniform sampler2D sampler;
uniform sampler2D bumpMap;

uniform sampler2D lightmap;

out vec4 out_color;
out vec4 out_brightness;

const float lightMin = 0.1;
const float lightScale = 0.9;

uniform sampler2D[4] shadowMap;

in vec3[4] pass_lightPos;
in vec3[4] pass_lightDir;
uniform vec2[4] cutoff;
uniform float[4] strength;

float ShadowCalculation(vec4 projLightSpace, int i) {
    vec3 projCoords = projLightSpace.xyz / projLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    float closestDepth = texture(shadowMap[i], projCoords.xy).r; 
    float currentDepth = projCoords.z;
   
	float bias = max(0.005 * (1.0 - dot(pass_normals, pass_lightDir[i])), 0.01);  
    float shadow = currentDepth < closestDepth + bias ? 0.0 : 1.0;

    return shadow;
} 

void main(void){
	vec4 color, light;
	vec3 normalMap;
	color = texture(sampler, pass_textureCoords.xy);
	
	vec4 normalMapValue;
	if (hasBumpMap != 0)
		normalMapValue = (2.0 * texture(bumpMap, pass_textureCoords.xy))- vec4(1.0);
	
	if (color.a < 0.5)
		discard;
	
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
		
		vec3 toLight = (pass_lightPos[i]);
		vec3 lightProjVec = normalize(toLight);
		
		vec3 lightDirComp = normalize(-pass_lightDir[i]);
		float dist = 1.0 + (length(toLight) / strength[i]);
		float theta = dot(lightProjVec, lightDirComp);
		float bumpFactor = 1.0;
		if (hasBumpMap != 0)
			bumpFactor = dot(lightProjVec, normalize(normalMapValue.xyz));
		float epsilon = cutoff[i].x - cutoff[i].y;
		
		intensity += bumpFactor * (clamp((theta - cutoff[i].y) / epsilon, 0.0, 1.0) / dist);
	}

	color.a = 1.0;
	out_color = ((color * (light + max(intensity, lightMin))) * lightScale);
	out_brightness = vec4(0.0);
}
