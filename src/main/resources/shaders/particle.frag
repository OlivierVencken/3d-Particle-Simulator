#version 430 core

in vec3 vColor;
out vec4 fragColor;

void main() {
    vec2 uv = gl_PointCoord * 2.0 - 1.0;
    float dist = dot(uv, uv);
    if (dist > 1.0) {
        discard;
    }

    float alpha = smoothstep(1.0, 0.05, dist);
    fragColor = vec4(vColor, alpha);
}
