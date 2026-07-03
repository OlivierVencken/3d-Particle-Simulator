#version 430 core

layout(lines) in;
layout(triangle_strip, max_vertices = 4) out;

uniform vec2 uViewport;

in vec3 vColor[];
in float vAlpha[];
in float vThickness[];

out vec3 fColor;
out float fAlpha;

void emitTrailVertex(int index, vec2 offset) {
    vec4 clip = gl_in[index].gl_Position;
    gl_Position = vec4(clip.xy + offset * clip.w, clip.zw);
    fColor = vColor[index];
    fAlpha = vAlpha[index] * 0.78;
    EmitVertex();
}

void main() {
    vec4 clip0 = gl_in[0].gl_Position;
    vec4 clip1 = gl_in[1].gl_Position;
    if (clip0.w <= 0.0 || clip1.w <= 0.0) {
        return;
    }

    vec2 ndc0 = clip0.xy / clip0.w;
    vec2 ndc1 = clip1.xy / clip1.w;
    vec2 direction = ndc1 - ndc0;
    float lengthSquared = dot(direction, direction);
    if (lengthSquared < 0.0000001) {
        return;
    }

    vec2 normal = normalize(vec2(-direction.y, direction.x));
    vec2 offset0 = normal * (vThickness[0] / uViewport);
    vec2 offset1 = normal * (vThickness[1] / uViewport);

    emitTrailVertex(0, offset0);
    emitTrailVertex(0, -offset0);
    emitTrailVertex(1, offset1);
    emitTrailVertex(1, -offset1);
    EndPrimitive();
}
