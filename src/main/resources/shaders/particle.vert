#version 430 core

layout(std430, binding = 0) readonly buffer Particles {
    vec4 positions[];
};

layout(std430, binding = 1) readonly buffer Velocities {
    vec4 velocities[];
};

layout(std430, binding = 2) readonly buffer GridCounts {
    int grid_counts[];
};

uniform mat4 uViewProjection;
uniform mat4 uView;
uniform float uPointSize;
uniform int uFixedParticleScreenSize;
uniform float uPointSizeReferenceDistance;
uniform int uColorMode;
uniform int uGroupCount;
uniform vec3 uGroupColors[16];
uniform float uMaxVelocity;
uniform float uBounds;
uniform float uInteractionRange;
uniform int uGridSize;

out vec3 vColor;

ivec3 getGridCoord(vec3 pos) {
    float inverseCellWidth = float(uGridSize) / (uBounds * 2.0);
    vec3 normalized = (pos + vec3(uBounds)) * inverseCellWidth;
    return clamp(ivec3(floor(normalized)), ivec3(0), ivec3(max(uGridSize - 1, 0)));
}

int getGridIndex(ivec3 coord) {
    return coord.x + uGridSize * (coord.y + uGridSize * coord.z);
}

void main() {
    vec4 particle = positions[gl_VertexID];
    vec3 position = particle.xyz;
    int group = int(mod(particle.w, float(max(uGroupCount, 1))));

    vec4 worldPosition = vec4(position, 1.0);
    vec4 viewPosition = uView * worldPosition;
    gl_Position = uViewProjection * worldPosition;

    if (uFixedParticleScreenSize == 1) {
        gl_PointSize = uPointSize;
    } else {
        float cameraDistance = max(0.1, length(viewPosition.xyz));
        gl_PointSize = clamp(uPointSize * (uPointSizeReferenceDistance / cameraDistance), 1.0, uPointSize * 8.0);
    }

    if (uColorMode == 0) {
        vColor = uGroupColors[group];
    } else if (uColorMode == 2) {
        // POSITION mode
        vec3 normalizedPos = (position + vec3(uBounds)) / (2.0 * uBounds);
        vColor = clamp(normalizedPos, 0.0, 1.0);
    } else if (uColorMode == 3) {
        // DISTANCE from center mode
        float dist = length(position);
        float normalizedDist = clamp(dist / (uBounds * 0.8), 0.0, 1.0);
        vColor = mix(vec3(1.0, 1.0, 0.5), vec3(0.05, 0.1, 0.4), normalizedDist);
    } else if (uColorMode == 4) {
        // DIRECTION mode
        vec3 velocity = velocities[gl_VertexID].xyz;
        float speed = length(velocity);
        vec3 direction = speed > 0.001 ? normalize(velocity) : vec3(0.0);
        vColor = direction * 0.5 + 0.5;
    } else if (uColorMode == 5) {
        // DENSITY mode
        ivec3 gridCoord = getGridCoord(position);
        int count = grid_counts[getGridIndex(gridCoord)];
        float normalizedDensity = clamp(float(count) / 30.0, 0.0, 1.0); // Arbitrary scaling factor
        vColor = mix(vec3(0.1, 0.2, 0.8), vec3(1.0, 0.1, 0.1), normalizedDensity);
    } else {
        vec3 velocity = velocities[gl_VertexID].xyz;
        float speed = length(velocity);
        float normalizedSpeed = clamp(speed / uMaxVelocity, 0.0, 1.0);
        vColor = mix(vec3(0.0, 0.0, 1.0), vec3(1.0, 0.0, 0.0), normalizedSpeed);
    }
}
