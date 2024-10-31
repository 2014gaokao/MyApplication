#include <jni.h>
#include <string>
#include <thread>
#include <iostream>

#include <android/log.h>

#include "EglHelper.h"

#include <android/bitmap.h>

//https://blog.csdn.net/cfc1243570631/article/details/135139836
//https://blog.csdn.net/qq_32708325/article/details/139812520
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_JNILoader_stringFromJNI(JNIEnv* env, jclass clazz, jobject bmp) {
    std::string hello = "Hello from C++";
    ALOGD("main thread id %u\n", std::this_thread::get_id());

    EglHelper eglHelper = *new EglHelper();
    eglHelper.initEgl();
    eglHelper.makeCurrent();

    AndroidBitmapInfo info;
    void *pixels;
    int ret;

    //jnigraphics
    if ((ret = AndroidBitmap_getInfo(env, bmp, &info)) < 0) {
        ALOGD("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return nullptr;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGD("Bitmap format is not RGBA_8888 !");
        return nullptr;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bmp, &pixels)) < 0) {
        ALOGD("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return nullptr;
    }

    uint32_t width = info.width;
    uint32_t height = info.height;

    AHardwareBuffer_Desc desc = {width, height,
                                 1,
                                 AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM,
                                 AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN | AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE,
                                 width,
                                 0, 0};
    AHardwareBuffer *inBuffer;
    ret = AHardwareBuffer_allocate(&desc, &inBuffer);

    AHardwareBuffer_Planes planes_info = {0};
    ret = AHardwareBuffer_lockPlanes(inBuffer, AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN, -1, nullptr, &planes_info);
    int rowStride = planes_info.planes[0].rowStride; //7680 7592
    ALOGD("rowStride stride width height: %d %d %d %d\n", rowStride, info.width * 4, info.width, info.height);
    //memcpy(planes_info.planes[0].data, pixels, width * height * 4);
    for (int i = 0; i < height; ++i) {
        memcpy((char *) planes_info.planes[0].data + i * rowStride, (char *) pixels + i * width * 4,
               rowStride < width * 4 ? rowStride : width * 4);
    }
    ret = AHardwareBuffer_unlock(inBuffer, nullptr);

    //https://github.com/fuyufjh/GraphicBuffer
    EGLClientBuffer clientBuf = eglGetNativeClientBufferANDROID(inBuffer);
    EGLDisplay disp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    EGLint eglImageAttributes[] = {EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE};
    EGLImageKHR imageEGL = eglCreateImageKHR(disp, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, clientBuf, eglImageAttributes);

    unsigned int fbo;
    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    //https://learnopengl-cn.readthedocs.io/zh/latest/04%20Advanced%20OpenGL/05%20Framebuffers/
    unsigned int textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, NULL);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, imageEGL);
    glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(GL_TEXTURE_2D, 0);

    AndroidBitmap_unlockPixels(env, bmp);

    ALOGD("fbo textureId %d %d\n", fbo, textureId);

    GLuint program = eglHelper.createProgram(VERTEX_SHADER, FRAG_SHADER_GREY);
    glBindAttribLocation(program, 0, "a_position");
    glBindAttribLocation(program, 1, "a_texCoord");

    glViewport(0, 0, width, height);

    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glUniform1i(glGetUniformLocation(program, "sTexture"), 0);
    glUniformMatrix4fv(glGetUniformLocation(program, "uMVPMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glUniformMatrix4fv(glGetUniformLocation(program, "uSTMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glVertexAttribPointer(0, 2, GL_FLOAT, 0, 0, vertex);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 2, GL_FLOAT, 1, 0, texture);
    glEnableVertexAttribArray(1);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glFinish();
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    //memcpy(pixels, planes_info.planes[0].data, width * height * 4);
    for (int i = 0; i < height; ++i) {
        memcpy((char *) pixels + i * width * 4, (char *) planes_info.planes[0].data + i * rowStride,
               rowStride < width * 4 ? rowStride : width * 4);
    }

    unsigned char *ptrReader = nullptr;
    unsigned char *dstBuffer = static_cast<unsigned char *>(malloc(width * height * 4));
    AHardwareBuffer_lock(inBuffer, AHARDWAREBUFFER_USAGE_CPU_READ_OFTEN, -1, nullptr, (void **) &ptrReader);
    memcpy(dstBuffer, ptrReader, width * height * 4);
    AHardwareBuffer_unlock(inBuffer, nullptr);
    ALOGD("dstBuffer %d %d %d %d\n", *dstBuffer, *(dstBuffer + 1), *(dstBuffer + 2), *(dstBuffer + 3));

    eglHelper.breakCurrent();

    return env->NewStringUTF(hello.c_str());
}