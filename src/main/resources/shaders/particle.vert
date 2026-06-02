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

uniform mat4 uViewProjection;
uniform mat4 uView;
uniform float uPointSize;
uniform int uFixedParticleScreenSize;
uniform float uPointSizeReferenceDistance;
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
        vColor = palette[group];
    }  else if (uColorMode == 2) {
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
        int gridIndex = findGridBucket(gridCoord);
        int count = gridIndex < 0 ? 0 : grid_counts[gridIndex];
        float normalizedDensity = clamp(float(count) / 30.0, 0.0, 1.0); // Arbitrary scaling factor
        vColor = mix(vec3(0.1, 0.2, 0.8), vec3(1.0, 0.1, 0.1), normalizedDensity);
    } else {
        vec3 velocity = velocities[gl_VertexID].xyz;
        float speed = length(velocity);
        float normalizedSpeed = clamp(speed / uMaxVelocity, 0.0, 1.0);
        vColor = mix(vec3(0.0, 0.0, 1.0), vec3(1.0, 0.0, 0.0), normalizedSpeed);
    }
}
