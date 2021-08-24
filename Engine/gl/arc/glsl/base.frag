#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;
const float shineDampen = 1.0;
const int MAX_LIGHTS = 4;

in vec4 pass_textureCoords;
in vec3 pass_normals;
in vec3 pass_worldPos;

uniform sampler2D sampler;
uniform sampler2D lightmap;

uniform sampler2D[MAX_LIGHTS] shadowMap;

in mat4 pass_lightPos;
in mat4 pass_lightDir;
in mat4 shadowCoords;

uniform vec4 lightInfo;

out vec4 out_color;

float ShadowCalculation(vec4 projLightSpace, int i) {
    vec3 projCoords = projLightSpace.xyz / projLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    float closestDepth = texture(shadowMap[i], projCoords.xy).r; 
    float currentDepth = projCoords.z;
   
	float bias = max(0.005 * (1.0 - dot(pass_normals, pass_lightDir[i].xyz)), 0.01);  
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
	
	float intensity = 0.0;
	float shadow = 0.0;
	
	for(int i = 0; i < MAX_LIGHTS; i++) {
		if (lightInfo[i] == 0.0)
			break;
		
		shadow = ShadowCalculation(shadowCoords[i], i); 
		
		if (shadow == 1.0)
			continue;
		
		vec3 lightProjVec = normalize(pass_lightPos[i].xyz);
		vec3 lightDirComp = -pass_lightDir[i].xyz;
		
		float dist = 1.0 + (length(pass_lightPos[i]) / lightInfo[i]);
		float theta = dot(lightProjVec, lightDirComp);

		float epsilon = (pass_lightPos[i].w - pass_lightDir[i].w);
		
		intensity += (clamp(((theta - pass_lightDir[i].w) / epsilon), 0.0, 1.0) / dist);
	
	}
	
	color.a = 1.0;
	out_color = (color * (light + max(intensity, lightMin))) * lightScale;
}
