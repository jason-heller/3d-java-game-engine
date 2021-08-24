#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;
const float shineDampen = 1.0;
const int MAX_LIGHTS = 4;

in vec4 pass_textureCoords;
in vec3 pass_normals;
in vec3 pass_worldPos;

uniform vec3 cameraPos;

uniform sampler2D sampler;
uniform sampler2D bumpMap;
uniform sampler2D specMap;
uniform sampler2D lightmap;
uniform samplerCube envMap;

uniform sampler2D[MAX_LIGHTS] shadowMap;

in mat4 pass_lightPos;
in mat4 pass_lightDir;
in vec3 specularUvs;
in mat4 shadowCoords;

uniform vec4 lightInfo;

out vec4 out_color;

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
	
	vec4 specularity = vec4(0.0);
	vec3 unitNormal = vec3(0,0,1);
	
	unitNormal = normalize(texture(bumpMap, pass_textureCoords.xy).rgb*2.0 - 1.0);
	
	if (color.a < 0.5)
		discard;
	
	light = texture(lightmap, pass_textureCoords.zw);
	
	// Spotlight
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
		
		vec3 reflectedLightDir = reflect(-lightDirComp, unitNormal);
		float specularityFactor = dot(reflectedLightDir, normalize(-pass_worldPos));
		
		specularityFactor = (texture(specMap, pass_textureCoords.xy).r*2.0 - 1.0);
		specularityFactor = max(specularityFactor, 0.0);
		
		float dampening = pow(specularityFactor, shineDampen);
		vec4 specColor = texture(envMap, specularUvs);
		specularity += (dampening * specColor) * intensity;
	}
	
	specularity.a = 0.0;
	color.a = 1.0;
	
	out_color = (color * (light + max(intensity, lightMin))) * lightScale;
	out_color += specularity;
}
