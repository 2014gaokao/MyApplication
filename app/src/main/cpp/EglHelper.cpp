#include "EglHelper.h"
#include <android/log.h>
#include <sstream>

#ifndef LOG_TAG
#define LOG_TAG "zcc c++"
#endif
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

EglHelper::EglHelper() {

}

void EglHelper::initEgl() {
    mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (mEglDisplay == EGL_NO_DISPLAY) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "eglGetDisplay error");
    }
    EGLint *version = new EGLint[2];
    if (!eglInitialize(mEglDisplay, &version[0], &version[1])) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "eglInitialize error");
    }

    const EGLint attrib_config_list[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 8,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_NONE
    };

    EGLint config;
    if (!eglChooseConfig(mEglDisplay, attrib_config_list, &mEglConfig, 1, &config)) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "eglChooseConfig error");
    }

    const EGLint attrib_ctx_list[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };
    mEglContext = eglCreateContext(mEglDisplay, mEglConfig, NULL, attrib_ctx_list);
    if (mEglContext == EGL_NO_CONTEXT) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "eglCreateContext error");
    }

    //mEglSurface = eglCreateWindowSurface(mEglDisplay, eglConfig, window, NULL);
    mEglSurface = eglCreatePbufferSurface(mEglDisplay, mEglConfig, NULL);
    if (mEglSurface == EGL_NO_SURFACE) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "eglCreateWindowSurface error");
    }

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Context init mEGLDisplay %p %p %p %d\n", mEglDisplay, mEglConfig, mEglContext, config);
}

void EglHelper::makeCurrent() {
    // 绑定eglContext和surface到display
    if (!eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "eglMakeCurrent error");
    }
}

void EglHelper::makeNothingCurrent() {
    eglMakeCurrent(mEglDisplay, NULL, NULL, NULL);
}

void EglHelper::test(int width, int height, void *pixels) {
    AHardwareBuffer_Desc desc;
    desc.format = AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM;
    desc.height = height;
    desc.width = width;
    desc.layers = 1;
    desc.rfu0 = 0;
    desc.rfu1 = 0;
    desc.usage = AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN | AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE;
    AHardwareBuffer *inBuffer;
    int ret = AHardwareBuffer_allocate(&desc, &inBuffer);
    ALOGD("ret: %d", ret);

    AHardwareBuffer_Planes planes_info = {0};
    ret = AHardwareBuffer_lockPlanes(inBuffer, AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN, -1, nullptr, &planes_info);
    memcpy(planes_info.planes[0].data, pixels, width * height * 4);
    ret = AHardwareBuffer_unlock(inBuffer, nullptr);

}

GLuint EglHelper::createInput(int width, int height) {
    AHardwareBuffer_Desc usage;
    usage.format = AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM;
    usage.height = height;
    usage.width = width;
    usage.layers = 1;
    usage.rfu0 = 0;
    usage.rfu1 = 0;
    usage.usage = AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN | AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE;
    //AHardwareBuffer *graphicBuf = NULL;
    AHardwareBuffer_allocate(&usage, &inputGraphicBuf);
    EGLClientBuffer clientBuf = eglGetNativeClientBufferANDROID(inputGraphicBuf);
    EGLint eglImageAttributes[] = {EGL_WIDTH, width, EGL_HEIGHT, height, EGL_NONE};
    EGLImageKHR imageEGL = eglCreateImageKHR(mEglDisplay, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, clientBuf, nullptr);
    glEGLImageTargetTexture2DOES(GL_TEXTURE_EXTERNAL_OES, imageEGL);
    //GLuint tex;
    glGenFramebuffers(1, &in_fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, in_fbo);

    glGenTextures(1, &inTextureId);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, inTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, NULL);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, inTextureId, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glFinish();

    ALOGD("tex fbo %d %d\n", inTextureId, in_fbo);
    return inTextureId;
}

void EglHelper::uploadData(int width, int height, void *plane0) {
    //glBindTexture(GL_TEXTURE_EXTERNAL_OES, inTextureId);
    AHardwareBuffer_Desc desc;
    AHardwareBuffer_describe(inputGraphicBuf, &desc);
    AHardwareBuffer_Planes planes_info;
    AHardwareBuffer_lockPlanes(inputGraphicBuf, AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN, -1, nullptr, &planes_info);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "desc.format %d\n", desc.format);
    //AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM
    AHardwareBuffer_Desc aHardwareBufferDescription = {};
    AHardwareBuffer_describe(inputGraphicBuf, &aHardwareBufferDescription);
    AHardwareBuffer_Plane *planes = planes_info.planes;
    int rowStride = planes[0].rowStride;
    int pixelStride = planes->pixelStride;
    int stride0 = width * 4;
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "rowStride: %d, pixelStride %d, stride0: %d\n", rowStride, pixelStride, stride0);
    if (stride0 != rowStride)
    {
//        for (int i = 0; i < height; i++) {
//            memcpy((char *) planes[0].data + i * rowStride, (char *) plane0 + i * stride0, rowStride < stride0 ? rowStride : stride0);
//        }
        memcpy(planes[0].data, plane0, stride0);
    }
    AHardwareBuffer_unlock(inputGraphicBuf, nullptr);

    //https://www.zybuluo.com/cxm-2016/note/544209
    glBindFramebuffer(GL_FRAMEBUFFER, rt_fbo);
    GLuint program = createProgram(VERTEX_SHADER, FRAG_SHADER);
    ALOGD("zcc program: %d\n", program);
    glBindAttribLocation(program, 0, "a_position");
    glBindAttribLocation(program, 1, "a_texCoord");
    glUseProgram(program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, inTextureId);
    glUniform1i(glGetUniformLocation(program, "sTexture"), 0);
    glUniformMatrix4fv(glGetUniformLocation(program, "uMVPMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glUniformMatrix4fv(glGetUniformLocation(program, "uSTMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glVertexAttribPointer(0, 2, GL_FLOAT, 0, 0, squareVertices);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 2, GL_FLOAT, 1, 0, squareUvs);
    glEnableVertexAttribArray(1);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glFinish();

    //debug only
    glBindFramebuffer(GL_FRAMEBUFFER, rt_fbo);
    unsigned char pixles[4];
    glReadPixels(0, 0, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixles);
    ALOGD("glreadpixel %u,%u,%u,%u",pixles[0],pixles[1],pixles[2],pixles[3]);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

bool EglHelper::checkGlError(const char* funcName) {
    GLint err = glGetError();
    if (err != GL_NO_ERROR) {
        ALOGE("GL error after %s(): 0x%08x\n", funcName, err);
        return true;
    }
    return false;
}

GLuint EglHelper::createShader(GLenum shaderType, const char *src) {
    GLuint shader = glCreateShader(shaderType);
    if (!shader) {
        checkGlError("glCreateShader");
        return 0;
    }
    glShaderSource(shader, 1, &src, NULL);
    GLint compiled = GL_FALSE;
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLogLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen > 0) {
            GLchar* infoLog = (GLchar*)malloc(infoLogLen);
            if (infoLog) {
                glGetShaderInfoLog(shader, infoLogLen, NULL, infoLog);
                ALOGE("Could not compile %s shader:\n%s\n",
                      shaderType == GL_VERTEX_SHADER ? "vertex" : "fragment",
                      infoLog);
                free(infoLog);
            }
        }
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

GLuint EglHelper::createProgram(const char* vtxSrc, const char* fragSrc) {
    GLuint vtxShader = 0;
    GLuint fragShader = 0;
    GLuint program = 0;
    GLint linked = GL_FALSE;
    vtxShader = createShader(GL_VERTEX_SHADER, vtxSrc);
    if (!vtxShader)
        goto exit;
    fragShader = createShader(GL_FRAGMENT_SHADER, fragSrc);
    if (!fragShader)
        goto exit;
    program = glCreateProgram();
    if (!program) {
        checkGlError("glCreateProgram");
        goto exit;
    }
    glAttachShader(program, vtxShader);
    glAttachShader(program, fragShader);
    glLinkProgram(program);
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        ALOGE("Could not link program");
        GLint infoLogLen = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen) {
            GLchar* infoLog = (GLchar*)malloc(infoLogLen);
            if (infoLog) {
                glGetProgramInfoLog(program, infoLogLen, NULL, infoLog);
                ALOGE("Could not link program:\n%s\n", infoLog);
                free(infoLog);
            }
        }
        glDeleteProgram(program);
        program = 0;
    }
    exit:
    glDeleteShader(vtxShader);
    glDeleteShader(fragShader);
    return program;
}

GLuint EglHelper::createOutput(int width, int height) {
    AHardwareBuffer_Desc usage;
    usage.format = AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM;
    usage.height = height;
    usage.width = width;
    usage.layers = 1;
    usage.rfu0 = 0;
    usage.rfu1 = 0;
    usage.usage = AHARDWAREBUFFER_USAGE_CPU_READ_OFTEN | AHARDWAREBUFFER_USAGE_GPU_COLOR_OUTPUT;
    //AHardwareBuffer *graphicBuf = NULL;
    AHardwareBuffer_allocate(&usage, &outputGraphicBuf);
    EGLClientBuffer clientBuf = eglGetNativeClientBufferANDROID(outputGraphicBuf);
    EGLint eglImageAttributes[] = {EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE};
    EGLImageKHR imageEGL = eglCreateImageKHR(mEglDisplay, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, clientBuf, eglImageAttributes);
    //GLuint tex;
    glGenTextures(1, &outTextureId);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, outTextureId);
    glEGLImageTargetTexture2DOES(GL_TEXTURE_EXTERNAL_OES, imageEGL);
    glFinish();
    //GLuint fbo;
    glGenFramebuffers(1, &rt_fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, rt_fbo);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "tex fbo %d %d\n", outTextureId, rt_fbo);
    glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, outTextureId, 0);
    return rt_fbo;
}

void EglHelper::downloadData(int w, int h, void *plane0) {

    AHardwareBuffer_Desc desc;
    AHardwareBuffer_describe(outputGraphicBuf, &desc);
    AHardwareBuffer_Planes planes_info;
    uint32_t width = desc.width;
    uint32_t height = desc.height;

    int result = AHardwareBuffer_lockPlanes(outputGraphicBuf, AHARDWAREBUFFER_USAGE_CPU_READ_OFTEN, -1, nullptr, &planes_info);
    AHardwareBuffer_Plane *planes = planes_info.planes;
    int stride = planes[0].rowStride;
    int stride0 = w * 4;
    if (stride0 != stride)
    {
//        for (int i = 0; i < height; i++) {
//            memcpy((char *) plane0 + i * stride0, (char *) planes[0].data + i * stride,
//                   stride  < stride0 ? stride : stride0);
//        }
        memcpy(plane0, planes[0].data, 30);
    }
    AHardwareBuffer_unlock(outputGraphicBuf, /*fence=*/NULL);
}

EglHelper::~EglHelper() {
    
}