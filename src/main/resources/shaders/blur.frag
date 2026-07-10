#version 430 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uDirection;
uniform float uRadius;
uniform float uFalloff;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(uTexture, 0));
    float spread = max(0.5, uRadius / sqrt(max(uFalloff, 0.05)));
    vec2 stepUv = uDirection * texel * spread;

    vec4 color = texture(uTexture, vUv) * 0.227027;
    color += texture(uTexture, vUv + stepUv * 1.384615) * 0.316216;
    color += texture(uTexture, vUv - stepUv * 1.384615) * 0.316216;
    color += texture(uTexture, vUv + stepUv * 3.230769) * 0.070270;
    color += texture(uTexture, vUv - stepUv * 3.230769) * 0.070270;
    fragColor = color;
}
