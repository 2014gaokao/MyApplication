#include "EglHelper.h"

class SplitRender {
public:
    SplitRender();
    ~SplitRender();
    void onSurfaceCreated();
    void onSurfaceChanged(int width, int height);
    void onDrawFrame(int textureId);

private:
    int program;
};

#define FRAG_SHADER_SPLIT \
"#version 300 es\n"             \
"#extension GL_OES_EGL_image_external_essl3 : require\n" \
"#extension GL_EXT_YUV_target : require\n"               \
"in vec2 v_texCoord;\n"         \
"uniform sampler2D sTexture;\n"                    \
"out vec4 outColor;\n"\
"void main()\n" \
"{\n"                     \
"   vec2 p = v_texCoord;" \
"   if (p.x < 0.5) {"     \
"       p.x = 1.0 - p.x;"     \
"   }"                          \
"   outColor = texture(sTexture, p);\n"        \
"}\n"