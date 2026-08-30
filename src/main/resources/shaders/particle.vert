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

layout(std430, binding = 9) readonly buffer ParticleGroups {
    int groups[];
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
uniform int uSimulationDimension;
uniform int uFourDVisualizationMode;
uniform mat4 uRotation4D;
uniform float uPerspectiveDistance;
uniform float uSliceCenterW;
uniform float uSliceThickness;
uniform float uSliceFeather;
uniform float uWColorRange;

out vec3 vColor;
out float vVisibilityAlpha;

const int DIMENSION_4D = 4;
const int VISUALIZATION_PERSPECTIVE = 0;
const int VISUALIZATION_SLICE = 1;
const int VISUALIZATION_W_COLOR = 2;
const float MAX_PERSPECTIVE_SCALE = 16.0;

ivec3 getGridCoord(vec3 pos) {
    float inverseCellWidth = float(uGridSize) / (uBounds * 2.0);
    vec3 normalized = (pos + vec3(uBounds)) * inverseCellWidth;
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
    if (feather <= 0.0) {
        return 1.0;
    }
    return 1.0 - smoothstep(halfThickness - feather, halfThickness, distanceFromCenter);
}

vec3 wPalette(float normalizedW) {
    const vec3 negativeW = vec3(0.12, 0.42, 1.0);
    const vec3 centerW = vec3(0.94, 0.94, 0.88);
    const vec3 positiveW = vec3(1.0, 0.25, 0.08);
    return normalizedW < 0.5
        ? mix(negativeW, centerW, normalizedW * 2.0)
        : mix(centerW, positiveW, (normalizedW - 0.5) * 2.0);
}

void main() {
    vec4 particle = positions[gl_VertexID];
    vec3 position = particle.xyz;
    int group = clamp(groups[gl_VertexID], 0, max(uGroupCount - 1, 0));

    float projectionScale = 1.0;
    vVisibilityAlpha = 1.0;
    bool visible = true;
    vec4 transformedParticle = particle;
    if (uSimulationDimension == DIMENSION_4D) {
        transformedParticle = uRotation4D * particle;
        visible = !any(isnan(transformedParticle)) && !any(isinf(transformedParticle));
        position = transformedParticle.xyz;

        if (visible && uFourDVisualizationMode == VISUALIZATION_PERSPECTIVE) {
            float denominator = uPerspectiveDistance - transformedParticle.w;
            float minimumDenominator = max(0.01, abs(uPerspectiveDistance) * 0.02);
            float fullOpacityDenominator = max(minimumDenominator * 2.0, abs(uPerspectiveDistance) * 0.15);
            if (denominator <= minimumDenominator || isnan(denominator) || isinf(denominator)) {
                visible = false;
            } else {
                float rawScale = uPerspectiveDistance / denominator;
                if (rawScale <= 0.0 || isnan(rawScale) || isinf(rawScale)) {
                    visible = false;
                } else {
                    projectionScale = min(rawScale, MAX_PERSPECTIVE_SCALE);
                    position *= projectionScale;
                    vVisibilityAlpha = smoothstep(minimumDenominator, fullOpacityDenominator, denominator);
                }
            }
        } else if (visible && uFourDVisualizationMode == VISUALIZATION_SLICE) {
            vVisibilityAlpha = sliceWeight(transformedParticle.w);
            visible = vVisibilityAlpha > 0.0;
        }
    }

    vec4 worldPosition = vec4(position, 1.0);
    vec4 viewPosition = uView * worldPosition;
    vec4 clipPosition = uViewProjection * worldPosition;
    if (uSimulationDimension == DIMENSION_4D) {
        visible = visible && !any(isnan(clipPosition)) && !any(isinf(clipPosition));
    }
    gl_Position = visible ? clipPosition : vec4(4.0, 4.0, 4.0, 1.0);

    if (uFixedParticleScreenSize == 1) {
        gl_PointSize = uSimulationDimension == DIMENSION_4D
                ? clamp(uPointSize * projectionScale, 1.0, uPointSize * 8.0)
                : uPointSize;
    } else {
        float cameraDistance = max(0.1, length(viewPosition.xyz));
        gl_PointSize = clamp(uPointSize * projectionScale * (uPointSizeReferenceDistance / cameraDistance),
                1.0, uPointSize * 8.0);
    }

    if (!visible) {
        vColor = vec3(0.0);
        return;
    }

    if (uSimulationDimension == DIMENSION_4D && uFourDVisualizationMode == VISUALIZATION_W_COLOR) {
        float normalizedW = clamp(0.5 + 0.5 * transformedParticle.w / uWColorRange, 0.0, 1.0);
        vColor = wPalette(normalizedW);
    } else if (uColorMode == 0) {
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
