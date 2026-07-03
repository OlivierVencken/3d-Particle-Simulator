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

layout(std430, binding = 3) readonly buffer GridKeys {
    int grid_keys[];
};

layout(std430, binding = 4) readonly buffer TrailHistory {
    vec4 history[];
};

uniform mat4 uViewProjection;
uniform mat4 uView;
uniform float uPointSize;
uniform int uFixedParticleScreenSize;
uniform float uPointSizeReferenceDistance;
uniform float uTrailThickness;
uniform int uParticleCount;
uniform int uParticleCapacity;
uniform int uSampleCapacity;
uniform int uNewestSampleIndex;
uniform int uSampleCount;
uniform int uColorMode;
uniform int uGroupCount;
uniform float uMaxVelocity;
uniform float uBounds;
uniform float uInteractionRange;
uniform int uGridSize;
uniform int uMapSize;

#define MAX_GRID_PROBES 32
#define GRID_KEY_COORD_OFFSET 512
#define GRID_KEY_COORD_MASK 1023

out vec3 vColor;
out float vAlpha;
out float vThickness;

ivec3 getGridCoord(vec3 pos) {
    vec3 normalized = (pos + vec3(uBounds)) / uInteractionRange;
    return clamp(ivec3(floor(normalized)), ivec3(0), ivec3(max(uGridSize - 1, 0)));
}

int getGridIndex(ivec3 coord) {
    uint h = uint(coord.x) * 73856093u;
    h ^= uint(coord.y) * 19349663u;
    h ^= uint(coord.z) * 83492791u;
    return int(h % uint(uMapSize));
}

int getGridKey(ivec3 coord) {
    ivec3 biased = coord + ivec3(GRID_KEY_COORD_OFFSET);
    if (any(lessThan(biased, ivec3(0))) || any(greaterThan(biased, ivec3(GRID_KEY_COORD_MASK)))) {
        return -1;
    }

    return 1
        + biased.x
        + biased.y * 1024
        + biased.z * 1048576;
}

int probeIndex(int startIndex, int probe) {
    int index = startIndex + probe;
    return index >= uMapSize ? index - uMapSize : index;
}

int findGridBucket(ivec3 coord) {
    int key = getGridKey(coord);
    if (key < 0) {
        return -1;
    }

    int startIndex = getGridIndex(coord);
    for (int probe = 0; probe < MAX_GRID_PROBES; probe++) {
        int index = probeIndex(startIndex, probe);
        int existingKey = grid_keys[index];
        if (existingKey == key) {
            return index;
        }
        if (existingKey == 0) {
            return -1;
        }
    }

    return -1;
}

vec3 particleColor(int particleId, vec3 position, int group) {
    if (uColorMode == 0) {
        vec3 palette[16] = vec3[](
            vec3(0.18, 0.65, 1.0),
            vec3(1.0, 0.35, 0.16),
            vec3(0.45, 1.0, 0.42),
            vec3(1.0, 0.86, 0.25),
            vec3(0.78, 0.42, 1.0),
            vec3(0.15, 0.95, 0.86),
            vec3(1.0, 0.45, 0.72),
            vec3(0.5, 0.95, 0.2),
            vec3(0.95, 0.62, 0.15),
            vec3(0.35, 0.55, 1.0),
            vec3(0.9, 0.95, 0.35),
            vec3(0.55, 0.25, 1.0),
            vec3(0.1, 0.8, 0.45),
            vec3(1.0, 0.2, 0.35),
            vec3(0.35, 1.0, 0.95),
            vec3(0.85, 0.85, 0.9)
        );
        return palette[group];
    } else if (uColorMode == 2) {
        vec3 normalizedPos = (position + vec3(uBounds)) / (2.0 * uBounds);
        return clamp(normalizedPos, 0.0, 1.0);
    } else if (uColorMode == 3) {
        float dist = length(position);
        float normalizedDist = clamp(dist / (uBounds * 0.8), 0.0, 1.0);
        return mix(vec3(1.0, 1.0, 0.5), vec3(0.05, 0.1, 0.4), normalizedDist);
    } else if (uColorMode == 4) {
        vec3 velocity = velocities[particleId].xyz;
        float speed = length(velocity);
        vec3 direction = speed > 0.001 ? normalize(velocity) : vec3(0.0);
        return direction * 0.5 + 0.5;
    } else if (uColorMode == 5) {
        ivec3 gridCoord = getGridCoord(position);
        int gridIndex = findGridBucket(gridCoord);
        int count = gridIndex < 0 ? 0 : grid_counts[gridIndex];
        float normalizedDensity = clamp(float(count) / 30.0, 0.0, 1.0);
        return mix(vec3(0.1, 0.2, 0.8), vec3(1.0, 0.1, 0.1), normalizedDensity);
    }

    vec3 velocity = velocities[particleId].xyz;
    float speed = length(velocity);
    float normalizedSpeed = clamp(speed / uMaxVelocity, 0.0, 1.0);
    return mix(vec3(0.0, 0.0, 1.0), vec3(1.0, 0.0, 0.0), normalizedSpeed);
}

void main() {
    int segmentVertex = gl_VertexID;
    int segmentIndex = segmentVertex / 2;
    int endpoint = segmentVertex - segmentIndex * 2;
    int particleId = segmentIndex % uParticleCount;
    int segmentAge = segmentIndex / uParticleCount;
    int age = segmentAge + endpoint;
    int sampleIndex = (uNewestSampleIndex - age + uSampleCapacity) % uSampleCapacity;

    vec4 particle = history[sampleIndex * uParticleCapacity + particleId];
    vec3 position = particle.xyz;
    int group = int(mod(positions[particleId].w, float(max(uGroupCount, 1))));

    vec4 worldPosition = vec4(position, 1.0);
    vec4 viewPosition = uView * worldPosition;
    gl_Position = uViewProjection * worldPosition;

    float particleScreenSize = uPointSize;
    if (uFixedParticleScreenSize != 1) {
        float cameraDistance = max(0.1, length(viewPosition.xyz));
        particleScreenSize = clamp(uPointSize * (uPointSizeReferenceDistance / cameraDistance), 1.0, uPointSize * 8.0);
    }

    vColor = particleColor(particleId, position, group);
    vAlpha = 1.0 - clamp(float(age) / float(max(uSampleCount - 1, 1)), 0.0, 1.0);
    vThickness = min(uTrailThickness, particleScreenSize);
}
