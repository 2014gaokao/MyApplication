#include "EglHelper.h"

class YUVRender {
public:
    YUVRender();
    ~YUVRender();
    void onSurfaceCreated();
    void onSurfaceChanged(int width, int height);
    void onDrawFrame(int textureId);

private:
    int program;
};

#define FRAG_SHADER_YUV \
"#version 300 es\n"             \
"#extension GL_OES_EGL_image_external_essl3 : require\n" \
"#extension GL_EXT_YUV_target : require\n"               \
"in vec2 v_texCoord;\n"         \
"uniform sampler2D sTexture;\n"                    \
"out vec4 outColor;\n"          \
"void main()\n" \
"{\n" \
"	outColor = texture(sTexture, v_texCoord);\n"     \
"   float weightMean = outColor.r * 0.3 + outColor.g * 0.59 + outColor.b * 0.11;\n" \
"   outColor.r = outColor.g = outColor.b = weightMean;\n"                    \
"}\n"