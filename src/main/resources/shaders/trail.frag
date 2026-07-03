#version 430 core

in vec3 fColor;
in float fAlpha;
out vec4 fragColor;

void main() {
    fragColor = vec4(fColor, clamp(fAlpha, 0.0, 1.0));
}
