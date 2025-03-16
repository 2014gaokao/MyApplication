#include "EglHelper.h"

class YUVPasteRender {
public:
    YUVPasteRender();
    ~YUVPasteRender();
    void onSurfaceCreated();
    void onSurfaceChanged(int width, int height);
    void onDrawFrame(int textureId, int pasteTextureId);

private:
    int program;
};

//https://www.shangyexinzhi.com/article/4712400.html
#define FRAG_SHADER_YUV \
"#version 300 es\n"             \
"#extension GL_OES_EGL_image_external_essl3 : require\n" \
"#extension GL_EXT_YUV_target : require\n"               \
"in vec2 v_texCoord;\n"         \
"uniform highp __samplerExternal2DY2YEXT sTexture;\n"    \
"uniform sampler2D pTexture;"                        \
"layout (yuv) out vec4 outColor;\n"                      \
                            \
"void main()\n" \
"{\n" \
"	vec4 color = texture(sTexture, v_texCoord);\n"         \
"   vec3 o_rgb = yuv_2_rgb(color.xyz,itu_601_full_range);\n"               \
"   vec4 p_rgb = texture(pTexture, v_texCoord);\n" \
"   o_rgb.rgb = p_rgb.rgb + o_rgb.rgb * (1.0 - p_rgb.a);\n"                            \
"   vec4 ret = vec4(clamp(o_rgb,0.0,1.0),1.0);\n"                    \
"   vec3 yuv = rgb_2_yuv(ret.xyz,itu_601_full_range).xyz;\n" \
"   outColor = vec4(yuv,1.0);\n"                    \
"}\n"