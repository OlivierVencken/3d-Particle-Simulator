#version 430 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uDirection;
uniform float uRadius;
uniform float uFalloff;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(uTexture, 0));
    vec2 stepUv = uDirection * texel * max(uRadius, 0.01);
    float falloff = max(uFalloff, 0.01);

    float centerWeight = 1.0;
    float nearWeight = exp(-1.384615 * falloff);
    float farWeight = exp(-3.230769 * falloff);
    float totalWeight = centerWeight + nearWeight * 2.0 + farWeight * 2.0;

    vec4 color = texture(uTexture, vUv) * centerWeight;
    color += texture(uTexture, vUv + stepUv * 1.384615) * nearWeight;
    color += texture(uTexture, vUv - stepUv * 1.384615) * nearWeight;
    color += texture(uTexture, vUv + stepUv * 3.230769) * farWeight;
    color += texture(uTexture, vUv - stepUv * 3.230769) * farWeight;

    fragColor = color / totalWeight;
}
