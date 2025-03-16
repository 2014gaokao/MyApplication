#include "EglHelper.h"

class YUVPasteRender {
public:
    YUVPasteRender();
    ~YUVPasteRender();
    void onSurfaceCreated();
    void onSurfaceChanged(int width, int height);
    void onDrawFrame(int textureId);

private:
    int program;
};

//https://www.shangyexinzhi.com/article/4712400.html
#define FRAG_SHADER_YUV \
"#version 300 es\n"             \
"#extension GL_OES_EGL_image_external_essl3 : require\n" \
"#extension GL_EXT_YUV_target : require\n"               \
"in vec2 v_texCoord;\n"         \
"uniform highp __samplerExternal2DY2YEXT sTexture;\n"                    \
"layout (yuv) out vec4 outColor;\n"                      \
                            \
"void main()\n" \
"{\n" \
"	vec4 color = texture(sTexture, v_texCoord);\n"         \
"   vec3 rgb = yuv_2_rgb(color.xyz,itu_601_full_range);\n"               \
"   float weightMean = rgb.r * 0.3 + rgb.g * 0.59 + rgb.b * 0.11;\n" \
"   rgb.r = rgb.g = rgb.b = weightMean;\n"                            \
"   vec4 ret = vec4(clamp(rgb,0.0,1.0),1.0);\n"                    \
"   vec3 yuv = rgb_2_yuv(ret.xyz,itu_601_full_range).xyz;\n" \
"   outColor = vec4(yuv,1.0);\n"                    \
"}\n"