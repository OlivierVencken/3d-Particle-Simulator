#version 430 core

layout(std430, binding = 0) readonly buffer Particles { vec4 positions[]; };
layout(std430, binding = 1) readonly buffer Velocities { vec4 velocities[]; };
layout(std430, binding = 2) readonly buffer GridCounts { int grid_counts[]; };
layout(std430, binding = 4) readonly buffer TrailHistory { vec4 history[]; };
layout(std430, binding = 9) readonly buffer ParticleGroups { int groups[]; };

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
uniform int uSimulationDimension;
uniform int uFourDVisualizationMode;
uniform mat4 uRotation4D;
uniform float uPerspectiveDistance;
uniform float uSliceCenterW;
uniform float uSliceThickness;
uniform float uSliceFeather;
uniform float uWColorRange;

out vec3 fColor;
out float fAlpha;

const int DIMENSION_4D = 4;
const int VISUALIZATION_PERSPECTIVE = 0;
const int VISUALIZATION_SLICE = 1;
const int VISUALIZATION_W_COLOR = 2;
const float MAX_PERSPECTIVE_SCALE = 16.0;

struct ProjectedPoint {
    vec4 source;
    vec4 transformed;
    vec3 visiblePosition;
    vec4 clip;
    float alpha;
    float scale;
    bool valid;
};

ivec3 getGridCoord(vec3 position) {
    float inverseCellWidth = float(uGridSize) / (uBounds * 2.0);
    vec3 normalized = (position + vec3(uBounds)) * inverseCellWidth;
    return clamp(ivec3(floor(normalized)), ivec3(0), ivec3(max(uGridSize - 1, 0)));
}

int getGridIndex(ivec3 coord) {
    return coord.x + uGridSize * (coord.y + uGridSize * coord.z);
}

float sliceWeight(float w) {
    float halfThickness = uSliceThickness * 0.5;
    float distanceFromCenter = abs(w - uSliceCenterW);
    if (distanceFromCenter >= halfThickness) {
        return 0.0;
    }
    float feather = min(uSliceFeather, halfThickness);
    return feather <= 0.0
            ? 1.0
            : 1.0 - smoothstep(halfThickness - feather, halfThickness, distanceFromCenter);
}

vec3 wPalette(float normalizedW) {
    const vec3 negativeW = vec3(0.12, 0.42, 1.0);
    const vec3 centerW = vec3(0.94, 0.94, 0.88);
    const vec3 positiveW = vec3(1.0, 0.25, 0.08);
    return normalizedW < 0.5
            ? mix(negativeW, centerW, normalizedW * 2.0)
            : mix(centerW, positiveW, (normalizedW - 0.5) * 2.0);
}

ProjectedPoint projectPoint(vec4 source) {
    ProjectedPoint point;
    point.source = source;
    point.transformed = source;
    point.visiblePosition = source.xyz;
    point.alpha = 1.0;
    point.scale = 1.0;
    point.valid = !any(isnan(source)) && !any(isinf(source));

    if (point.valid && uSimulationDimension == DIMENSION_4D) {
        point.transformed = uRotation4D * source;
        point.visiblePosition = point.transformed.xyz;
        point.valid = !any(isnan(point.transformed)) && !any(isinf(point.transformed));
        if (point.valid && uFourDVisualizationMode == VISUALIZATION_PERSPECTIVE) {
            float denominator = uPerspectiveDistance - point.transformed.w;
            float minimumDenominator = max(0.01, abs(uPerspectiveDistance) * 0.02);
            float fullOpacityDenominator = max(minimumDenominator * 2.0,
                    abs(uPerspectiveDistance) * 0.15);
            point.valid = denominator > minimumDenominator && !isnan(denominator) && !isinf(denominator);
            if (point.valid) {
                float rawScale = uPerspectiveDistance / denominator;
                point.valid = rawScale > 0.0 && !isnan(rawScale) && !isinf(rawScale);
                if (point.valid) {
                    point.scale = min(rawScale, MAX_PERSPECTIVE_SCALE);
                    point.visiblePosition *= point.scale;
                    point.alpha = smoothstep(minimumDenominator, fullOpacityDenominator, denominator);
                }
            }
        } else if (point.valid && uFourDVisualizationMode == VISUALIZATION_SLICE) {
            point.alpha = sliceWeight(point.transformed.w);
        }
    }

    point.clip = uViewProjection * vec4(point.visiblePosition, 1.0);
    point.valid = point.valid && point.clip.w > 0.0
            && !any(isnan(point.clip)) && !any(isinf(point.clip));
    return point;
}

vec3 particleColor(int particleId, ProjectedPoint point, int group) {
    if (uSimulationDimension == DIMENSION_4D && uFourDVisualizationMode == VISUALIZATION_W_COLOR) {
        float normalizedW = clamp(0.5 + 0.5 * point.transformed.w / uWColorRange, 0.0, 1.0);
        return wPalette(normalizedW);
    }
    if (uColorMode == 0) {
        vec3 palette[16] = vec3[](
            vec3(0.18, 0.65, 1.0), vec3(1.0, 0.35, 0.16),
            vec3(0.45, 1.0, 0.42), vec3(1.0, 0.86, 0.25),
            vec3(0.78, 0.42, 1.0), vec3(0.15, 0.95, 0.86),
            vec3(1.0, 0.45, 0.72), vec3(0.5, 0.95, 0.2),
            vec3(0.95, 0.62, 0.15), vec3(0.35, 0.55, 1.0),
            vec3(0.9, 0.95, 0.35), vec3(0.55, 0.25, 1.0),
            vec3(0.1, 0.8, 0.45), vec3(1.0, 0.2, 0.35),
            vec3(0.35, 1.0, 0.95), vec3(0.85, 0.85, 0.9));
        return palette[group];
    }
    if (uColorMode == 2) {
        return clamp((point.transformed.xyz + vec3(uBounds)) / (2.0 * uBounds), 0.0, 1.0);
    }
    if (uColorMode == 3) {
        float distance = uSimulationDimension == DIMENSION_4D
                ? length(point.transformed)
                : length(point.transformed.xyz);
        float normalizedDistance = clamp(distance / (uBounds * 0.8), 0.0, 1.0);
        return mix(vec3(1.0, 1.0, 0.5), vec3(0.05, 0.1, 0.4), normalizedDistance);
    }
    if (uColorMode == 4) {
        vec4 velocity = velocities[particleId];
        if (uSimulationDimension == DIMENSION_4D) {
            velocity = uRotation4D * velocity;
        } else {
            velocity.w = 0.0;
        }
        float speed = length(velocity);
        return (speed > 0.001 ? velocity.xyz / speed : vec3(0.0)) * 0.5 + 0.5;
    }
    if (uColorMode == 5) {
        int count = grid_counts[getGridIndex(getGridCoord(point.source.xyz))];
        float normalizedDensity = clamp(float(count) / 30.0, 0.0, 1.0);
        return mix(vec3(0.1, 0.2, 0.8), vec3(1.0, 0.1, 0.1), normalizedDensity);
    }

    vec4 velocity = velocities[particleId];
    float speed = uSimulationDimension == DIMENSION_4D ? length(velocity) : length(velocity.xyz);
    float normalizedSpeed = clamp(speed / uMaxVelocity, 0.0, 1.0);
    return mix(vec3(0.0, 0.0, 1.0), vec3(1.0, 0.0, 0.0), normalizedSpeed);
}

void hideSegment() {
    gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    fColor = vec3(0.0);
    fAlpha = 0.0;
}

void main() {
    int segmentAge = gl_InstanceID / uRenderedParticleCount;
    int particleOrdinal = gl_InstanceID - segmentAge * uRenderedParticleCount;
    int particleId = min(particleOrdinal * uParticleStride, uParticleCount - 1);

    int firstSample = (uNewestSampleIndex - segmentAge + uSampleCapacity) % uSampleCapacity;
    int secondSample = (uNewestSampleIndex - segmentAge - 1 + uSampleCapacity * 2) % uSampleCapacity;
    ProjectedPoint first = projectPoint(history[firstSample * uParticleCapacity + particleId]);
    ProjectedPoint second = projectPoint(history[secondSample * uParticleCapacity + particleId]);

    bool sliceOutside = uSimulationDimension == DIMENSION_4D
            && uFourDVisualizationMode == VISUALIZATION_SLICE
            && first.alpha <= 0.0 && second.alpha <= 0.0;
    if (!first.valid || !second.valid || sliceOutside) {
        hideSegment();
        return;
    }

    vec2 direction = second.clip.xy / second.clip.w - first.clip.xy / first.clip.w;
    if (dot(direction, direction) < 0.0000001) {
        hideSegment();
        return;
    }

    int endpointByVertex[6] = int[](0, 0, 1, 1, 0, 1);
    float sideByVertex[6] = float[](1.0, -1.0, 1.0, 1.0, -1.0, -1.0);
    int endpoint = endpointByVertex[gl_VertexID];
    ProjectedPoint point = first;
    if (endpoint == 1) {
        point = second;
    }
    vec3 viewPosition = (uView * vec4(point.visiblePosition, 1.0)).xyz;

    float screenSize = uPointSize * point.scale;
    if (uFixedParticleScreenSize != 1) {
        float cameraDistance = max(0.1, length(viewPosition));
        screenSize = clamp(screenSize * (uPointSizeReferenceDistance / cameraDistance),
                1.0, uPointSize * 8.0);
    }
    float thickness = min(uTrailThickness * point.scale, screenSize);
    vec2 normal = normalize(vec2(-direction.y, direction.x));
    vec2 offset = normal * (thickness / uViewport) * sideByVertex[gl_VertexID];
    gl_Position = vec4(point.clip.xy + offset * point.clip.w, point.clip.zw);

    int group = clamp(groups[particleId], 0, max(uGroupCount - 1, 0));
    fColor = particleColor(particleId, point, group);
    float age = float(segmentAge + endpoint);
    float ageAlpha = 1.0 - clamp(age / float(max(uSampleCount - 1, 1)), 0.0, 1.0);
    fAlpha = ageAlpha * 0.78 * point.alpha;
}
