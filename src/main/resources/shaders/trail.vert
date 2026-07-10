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

layout(std430, binding = 4) readonly buffer TrailHistory {
    vec4 history[];
};

uniform mat4 uViewProjection;
uniform mat4 uView;
uniform vec2 uViewport;
uniform float uPointSize;
uniform int uFixedParticleScreenSize;
uniform float uPointSizeReferenceDistance;
uniform float uTrailThickness;
uniform int uParticleCount;
uniform int uParticleCapacity;
uniform int uSampleCapacity;
uniform int uNewestSampleIndex;
uniform int uSampleCount;
uniform int uRenderedParticleCount;
uniform int uParticleStride;
uniform int uColorMode;
uniform int uGroupCount;
uniform float uMaxVelocity;
uniform float uBounds;
uniform float uInteractionRange;
uniform int uGridSize;

out vec3 fColor;
out float fAlpha;

ivec3 getGridCoord(vec3 position) {
    vec3 normalized = (position + vec3(uBounds)) / uInteractionRange;
    return clamp(ivec3(floor(normalized)), ivec3(0), ivec3(max(uGridSize - 1, 0)));
}

int getGridIndex(ivec3 coord) {
    return coord.x + uGridSize * (coord.y + uGridSize * coord.z);
}

vec3 particleColor(int particleId, vec3 position, int group) {
    if (uColorMode == 0) {
        vec3 palette[16] = vec3[](
            vec3(0.18, 0.65, 1.0), vec3(1.0, 0.35, 0.16),
            vec3(0.45, 1.0, 0.42), vec3(1.0, 0.86, 0.25),
            vec3(0.78, 0.42, 1.0), vec3(0.15, 0.95, 0.86),
            vec3(1.0, 0.45, 0.72), vec3(0.5, 0.95, 0.2),
            vec3(0.95, 0.62, 0.15), vec3(0.35, 0.55, 1.0),
            vec3(0.9, 0.95, 0.35), vec3(0.55, 0.25, 1.0),
            vec3(0.1, 0.8, 0.45), vec3(1.0, 0.2, 0.35),
            vec3(0.35, 1.0, 0.95), vec3(0.85, 0.85, 0.9)
        );
        return palette[group];
    }
    if (uColorMode == 2) {
        return clamp((position + vec3(uBounds)) / (2.0 * uBounds), 0.0, 1.0);
    }
    if (uColorMode == 3) {
        float normalizedDistance = clamp(length(position) / (uBounds * 0.8), 0.0, 1.0);
        return mix(vec3(1.0, 1.0, 0.5), vec3(0.05, 0.1, 0.4), normalizedDistance);
    }
    if (uColorMode == 4) {
        vec3 velocity = velocities[particleId].xyz;
        float speed = length(velocity);
        return (speed > 0.001 ? normalize(velocity) : vec3(0.0)) * 0.5 + 0.5;
    }
    if (uColorMode == 5) {
        int count = grid_counts[getGridIndex(getGridCoord(position))];
        float normalizedDensity = clamp(float(count) / 30.0, 0.0, 1.0);
        return mix(vec3(0.1, 0.2, 0.8), vec3(1.0, 0.1, 0.1), normalizedDensity);
    }

    float normalizedSpeed = clamp(length(velocities[particleId].xyz) / uMaxVelocity, 0.0, 1.0);
    return mix(vec3(0.0, 0.0, 1.0), vec3(1.0, 0.0, 0.0), normalizedSpeed);
}

void main() {
    int segmentAge = gl_InstanceID / uRenderedParticleCount;
    int particleOrdinal = gl_InstanceID - segmentAge * uRenderedParticleCount;
    int particleId = min(particleOrdinal * uParticleStride, uParticleCount - 1);

    int firstSample = (uNewestSampleIndex - segmentAge + uSampleCapacity) % uSampleCapacity;
    int secondSample = (uNewestSampleIndex - segmentAge - 1 + uSampleCapacity * 2) % uSampleCapacity;
    vec3 firstPosition = history[firstSample * uParticleCapacity + particleId].xyz;
    vec3 secondPosition = history[secondSample * uParticleCapacity + particleId].xyz;
    vec4 firstClip = uViewProjection * vec4(firstPosition, 1.0);
    vec4 secondClip = uViewProjection * vec4(secondPosition, 1.0);

    if (firstClip.w <= 0.0 || secondClip.w <= 0.0) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        fColor = vec3(0.0);
        fAlpha = 0.0;
        return;
    }

    vec2 direction = secondClip.xy / secondClip.w - firstClip.xy / firstClip.w;
    float directionLengthSquared = dot(direction, direction);
    if (directionLengthSquared < 0.0000001) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        fColor = vec3(0.0);
        fAlpha = 0.0;
        return;
    }

    int endpointByVertex[6] = int[](0, 0, 1, 1, 0, 1);
    float sideByVertex[6] = float[](1.0, -1.0, 1.0, 1.0, -1.0, -1.0);
    int endpoint = endpointByVertex[gl_VertexID];
    float side = sideByVertex[gl_VertexID];
    vec3 position = endpoint == 0 ? firstPosition : secondPosition;
    vec4 clip = endpoint == 0 ? firstClip : secondClip;
    vec3 viewPosition = (uView * vec4(position, 1.0)).xyz;

    float screenSize = uPointSize;
    if (uFixedParticleScreenSize != 1) {
        float cameraDistance = max(0.1, length(viewPosition));
        screenSize = clamp(uPointSize * (uPointSizeReferenceDistance / cameraDistance), 1.0, uPointSize * 8.0);
    }
    float thickness = min(uTrailThickness, screenSize);
    vec2 normal = normalize(vec2(-direction.y, direction.x));
    vec2 offset = normal * (thickness / uViewport) * side;
    gl_Position = vec4(clip.xy + offset * clip.w, clip.zw);

    int group = int(mod(positions[particleId].w, float(max(uGroupCount, 1))));
    fColor = particleColor(particleId, position, group);
    float age = float(segmentAge + endpoint);
    fAlpha = (1.0 - clamp(age / float(max(uSampleCount - 1, 1)), 0.0, 1.0)) * 0.78;
}
