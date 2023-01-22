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
	pass_normal = totalNormal.xyz;
	pass_textureCoords = in_textureCoords;
	toCamera = normalize(cameraPos.xyz - posTransformed.xyz);
	
	vec3 worldNormal = (mat3(modelMatrix) * totalNormal.xyz);
	vec3 nSqr = worldNormal * worldNormal;
	ivec3 isNegative = ivec3(worldNormal.x < 0.0, worldNormal.y < 0.0, worldNormal.z < 0.0);
	lightColor = vec3(
		nSqr.x * lights[isNegative.x] +
		nSqr.y * lights[isNegative.y + 2] +
		nSqr.z * lights[isNegative.z + 4]
	);
}
