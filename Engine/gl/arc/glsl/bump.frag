#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;
const float shineDampen = 1.0;
const int MAX_LIGHTS = 4;

in vec4 pass_textureCoords;
in vec3 pass_worldPos;

uniform sampler2D sampler;
uniform sampler2D bumpMap;
uniform sampler2D lightmap;

uniform sampler2D[MAX_LIGHTS] shadowMap;

in mat4 pass_lightPos;
in mat4 pass_lightDir;
in mat4 shadowCoords;

uniform vec4 lightInfo;
uniform mat3 lightStyles;

out vec4 outputColor;

float ShadowCalculation(vec4 projLightSpace, vec3 normal, int i) {
    vec3 projCoords = projLightSpace.xyz / projLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    float closestDepth = texture(shadowMap[i], projCoords.xy).r; 
    float currentDepth = projCoords.z;
   
	float bias = max(0.005 * (1.0 - dot(normal, pass_lightDir[i].xyz)), 0.01);  
    float shadow = currentDepth < closestDepth + bias ? 0.0 : 1.0;

    return shadow;
} 

void main(void){
	vec4 color, light;
	vec3 normalMap;
	color = texture(sampler, pass_textureCoords.xy);
	
	vec3 unitNormal = normalize(texture(bumpMap, pass_textureCoords.xy).rgb * 2.0 - 1.0);
	
	if (color.a < 0.5)
		discard;
	
	light = texture(lightmap, pass_textureCoords.zw);
	for(int i = 0; i < 3; i++) {
		light += texture(lightmap, pass_textureCoords.zw + lightStyles[i].xy) * lightStyles[i].z;
	}
	
	float intensity = 0.0;
	float shadow = 0.0;
	float brightness = 1.0;
	
	for(int i = 0; i < MAX_LIGHTS; i++) {
		if (lightInfo[i] == 0.0)
			break;
		
		shadow = ShadowCalculation(shadowCoords[i], unitNormal, i); 
		
		if (shadow == 1.0)
			continue;
		
		vec3 lightProjVec = normalize(pass_lightPos[i].xyz);
		vec3 lightDirComp = -pass_lightDir[i].xyz;
		
		float dist = 1.0 + (length(pass_lightPos[i]) / lightInfo[i]);
		float theta = dot(lightProjVec, lightDirComp);
		
		brightness = max(dot(lightDirComp, unitNormal), 0.0);
			
		float epsilon = (pass_lightPos[i].w - pass_lightDir[i].w);
		
		intensity += (clamp(((theta - pass_lightDir[i].w) / epsilon) * brightness, 0.0, 1.0) / dist);
	
	}
	
	color.a = 1.0;
	outputColor = (color * (light + max(intensity, lightMin))) * lightScale;
}
