#version 430 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uScene;

void main() {
    vec3 scene = texture(uScene, vUv).rgb;
    float brightness = max(max(scene.r, scene.g), scene.b);
    float contribution = smoothstep(0.25, 0.8, brightness);
    fragColor = vec4(scene * contribution, contribution);
}
