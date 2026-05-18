#version 430 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uScene;
uniform sampler2D uBloom;
uniform float uBloomStrength;

void main() {
    vec4 scene = texture(uScene, vUv);
    vec3 bloom = texture(uBloom, vUv).rgb * uBloomStrength;
    fragColor = vec4(scene.rgb + bloom, max(scene.a, 1.0));
}
