#version 320 es

layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 texCoord;

out vec2 aTexCoord;

uniform mat4 uTexMatrix;
uniform mat4 uMVPMatrix;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    aTexCoord = (uTexMatrix * vec4(texCoord, 0.0, 1.0)).xy;
}