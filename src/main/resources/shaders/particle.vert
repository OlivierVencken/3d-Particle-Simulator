#version 430 core

layout(std430, binding = 0) readonly buffer Particles {
    vec4 positions[];
};

uniform mat4 uViewProjection;
uniform float uPointSize;

out vec3 vColor;

void main() {
    vec4 particle = positions[gl_VertexID];
    vec3 position = particle.xyz;
    float group = mod(particle.w, 6.0);

    gl_Position = uViewProjection * vec4(position, 1.0);
    gl_PointSize = uPointSize;

    vec3 palette[6] = vec3[](
        vec3(0.18, 0.65, 1.0),
        vec3(1.0, 0.35, 0.16),
        vec3(0.45, 1.0, 0.42),
        vec3(1.0, 0.86, 0.25),
        vec3(0.78, 0.42, 1.0),
        vec3(0.15, 0.95, 0.86)
    );
    vColor = palette[int(group)];
}
