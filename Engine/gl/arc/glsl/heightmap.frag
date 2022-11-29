#version 150

const float lightMin = 0.1;
const float lightScale = 0.9;
const int MAX_LIGHTS = 4;

in vec4 pass_textureCoords;
in float pass_blends;
in vec3 pass_worldPos;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D lightmap;
uniform vec3 lightDirection;

out vec4 out_color;
out vec4 out_brightness;

in mat4 pass_lightPos;
uniform mat4 lightDir;
in mat4 shadowCoords;

uniform vec4 lightInfo;
uniform mat3 lightStyles;

uniform sampler2D[MAX_LIGHTS] shadowMap;

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

	color.a = 1.0;
	out_color = ((color * (light + max(intensity, lightMin))) * lightScale);
}
