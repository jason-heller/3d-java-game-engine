#version 150

const int MAX_JOINTS = 50;//max joints allowed in a skeleton
const int MAX_WEIGHTS = 3;//max number of joints that can affect a vertex

in vec3 in_position;
in vec2 in_textureCoords;
in vec3 in_normal;
in ivec3 in_jointIndices;
in vec3 in_weights;


out vec2 pass_textureCoords;
out vec3 pass_normal;
out vec3 toCamera;
out vec3 lightColor;

uniform mat4 modelMatrix;
uniform mat3 invTransRotMatrix;
uniform mat4 jointTransforms[MAX_JOINTS];
uniform mat4 projectionViewMatrix;
uniform vec3 lights[6];


const vec3 lightNormals[6] = vec3[6](vec3(1.0,0.0,0.0), vec3(-1.0,0.0,0.0), vec3(0.0,1.0,0.0), vec3(0.0,-1.0,0.0), vec3(0.0,0.0,1.0), vec3(0.0,0.0,-1.0));

uniform vec3 cameraPos;

void main(void){
	
	vec4 totalLocalPos = vec4(0.0);
	vec4 totalNormal = vec4(0.0);
	
	for(int i = 0; i < MAX_WEIGHTS; i++) {
		mat4 jointTransform = jointTransforms[in_jointIndices[i]];
		vec4 posePosition = jointTransform * vec4(in_position, 1.0);
		totalLocalPos += posePosition * in_weights[i];
		
		vec4 worldNormal = jointTransform * vec4(in_normal, 0.0);
		totalNormal += worldNormal * in_weights[i];
	}

	vec4 posTransformed = modelMatrix * totalLocalPos;
	
	gl_Position = projectionViewMatrix * posTransformed;
	pass_normal = invTransRotMatrix * totalNormal.xyz;
	pass_textureCoords = in_textureCoords;
	toCamera = normalize(cameraPos.xyz - posTransformed.xyz);
	
	lightColor = vec3(0.0);
	vec3 modelSpaceNormals = pass_normal;
	
	float totalWeight = 0.0;
	for(int i = 0; i < 6; ++i) {
		float weight = dot(modelSpaceNormals, lightNormals[i]);
		if (weight <= 0.0)
			continue;
		lightColor += lights[i] * weight;
		totalWeight += weight;
	}
	lightColor /= totalWeight;
}