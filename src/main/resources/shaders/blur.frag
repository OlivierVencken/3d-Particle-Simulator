#version 430 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uDirection;
uniform float uRadius;
uniform float uFalloff;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(uTexture, 0));
    vec2 stepUv = uDirection * texel;
    float sigma = max(uRadius, 0.5);
    float falloff = max(uFalloff, 0.01);

    vec4 color = vec4(0.0);
    float totalWeight = 0.0;

    for (int offset = -8; offset <= 8; offset++) {
        float distance = float(offset);
        float weight = exp(-0.5 * falloff * distance * distance / (sigma * sigma));
        color += texture(uTexture, vUv + stepUv * distance) * weight;
        totalWeight += weight;
    }

    fragColor = color / totalWeight;
}
