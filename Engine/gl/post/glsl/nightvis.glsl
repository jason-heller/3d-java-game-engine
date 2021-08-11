#version 150

in vec2 pass_textureCoords;
uniform sampler2D sampler;
uniform float timer;
uniform float noiseAmplifier;

out vec4 out_color;

uniform sampler2D noiseSampler;

float contrast = 0.5;

void main(void){
	vec2 uv;
	uv.x = 0.35*sin(timer*50.0);
	uv.y = 0.35*cos(timer*50.0);   
	
	vec3 noise = texture(noiseSampler, pass_textureCoords.st*2.0 + uv).rgb * noiseAmplifier;
	
	// frame buffer + litle swirl
	vec3 sceneColor = texture(sampler, pass_textureCoords.st).rgb * 2.0;    
	
	//color intensity - color dominant
	const vec3 lumvec = vec3(0.30, 0.59, 0.11);
	float intentisy = dot(lumvec,sceneColor) ;
	 
	// adjust contrast - 0...1
	intentisy = clamp(contrast * (intentisy - 0.5) + 0.5, 0.0, 1.0);
	 
	// final green result 0...1
	float green = clamp(intentisy / 0.59, 0.0, 1.0) * 1.0;
	 
	// vision color - getting green max
	vec3 visionColor = vec3(0.2,green,0.2);
	
	// final color
	out_color = vec4(((sceneColor+vec3(0.25)*2.0) - (noise*0.14)) * visionColor, 1.0);
}