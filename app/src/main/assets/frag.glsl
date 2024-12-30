#version 320 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

uniform samplerExternalOES vTexture;

in vec2 aTexCoord;

out vec4 FragColor;
void main() {
    FragColor = texture(vTexture, aTexCoord);
}