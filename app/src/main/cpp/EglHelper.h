#define GL_GLEXT_PROTOTYPES
#define EGL_EGLEXT_PROTOTYPES
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>

#include <android/hardware_buffer.h>

#include <string>

#ifndef LOG_TAG
#define LOG_TAG "zcc c++"
#endif
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

class EglHelper {
public:
    EGLDisplay mEglDisplay;
    EGLContext mEglContext;
    EGLSurface mEglSurface;
    EGLConfig mEglConfig;

    AHardwareBuffer *inputGraphicBuf = NULL;
    GLuint inTextureId = 0;
    GLuint in_fbo;

    AHardwareBuffer *outputGraphicBuf = NULL;
    GLuint outTextureId;
    GLuint rt_fbo;

    EglHelper();
    ~EglHelper();

    //检查当前程序错误
    bool checkGlError(const char* funcName);
    //获取并编译着色器对象
    GLuint createShader(GLenum shaderType, const char* src);
    //使用着色器生成着色器程序对象
    GLuint createProgram(const char* vtxSrc, const char* fragSrc);

    void initEgl();
    void makeCurrent();
    void makeNothingCurrent();
    void swapBuffers();

};

const GLfloat squareVertices[] = {-1, 1, -1, -1, 1, 1, 1, -1};

const GLfloat squareUvs[] = {0, 1, 0, 0, 1, 1, 1, 0};

const GLfloat IDENTITY_MATRIX[16] = {
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
};

#define VERTEX_SHADER \
"#version 300 es\n"   \
"layout (location = 0) in vec4 a_position;\n" \
"layout (location = 1) in vec2 a_texCoord;\n" \
"out vec2 v_texCoord;\n"      \
"precision highp float;\n"                         \
"uniform mat4 uMVPMatrix; \n" \
"uniform mat4 uSTMatrix; \n"                    \
"void main()\n" \
"{\n" \
"	v_texCoord = (uSTMatrix * vec4(a_texCoord, 0, 1)).st;\n" \
"   gl_Position = uMVPMatrix * a_position;\n" \
"}\n"


#define FRAG_SHADER \
"#version 300 es\n"             \
"#extension GL_OES_EGL_image_external_essl3 : require\n" \
"#extension GL_EXT_YUV_target : require\n"          \
"#define gl_FragColor outColor\n"                  \
"in vec2 v_texCoord;\n"         \
"uniform sampler2D sTexture;"                    \
"out vec4 gl_FragColor;\n" \
"void main()\n" \
"{\n" \
"	gl_FragColor = texture(sTexture, v_texCoord);\n"     \
"   float average = 0.21 * gl_FragColor.r + 0.71 * gl_FragColor.g + 0.07 * gl_FragColor.b;\n" \
"   gl_FragColor = vec4(average, average, average, 1.0);\n"                    \
"}\n"