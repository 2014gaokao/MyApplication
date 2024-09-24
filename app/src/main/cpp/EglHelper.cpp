#include "EglHelper.h"
#include <android/log.h>
#include <sstream>

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

EglHelper::~EglHelper() {
    
}