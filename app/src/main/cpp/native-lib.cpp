#include <jni.h>
#include <string>
#include <thread>
#include <iostream>

#include <android/log.h>

#include "GrayRender.h"
#include "SplitRender.h"
#include "YUVRender.h"
#include "YUVPasteRender.h"

#include <android/bitmap.h>

#include "include/turbojpeg.h"

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

    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    GrayRender grayRender = *new GrayRender();
    grayRender.onSurfaceCreated();
    grayRender.onSurfaceChanged(width, height);
    grayRender.onDrawFrame(textureId);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glFinish();

//    unsigned int test_fbo;
//    glGenFramebuffers(1, &test_fbo);
//    glBindFramebuffer(GL_FRAMEBUFFER, test_fbo);
//    unsigned int test_textureId;
//    glGenTextures(1, &test_textureId);
//    glBindTexture(GL_TEXTURE_2D, test_textureId);
//    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, NULL);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//    glBindFramebuffer(GL_FRAMEBUFFER, test_fbo);
//    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
//    glBindFramebuffer(GL_FRAMEBUFFER, 0);
//    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
//    SplitRender splitRender = *new SplitRender();
//    splitRender.onSurfaceCreated();
//    splitRender.onSurfaceChanged(width, height);
//    splitRender.onDrawFrame(textureId);
//    glBindFramebuffer(GL_FRAMEBUFFER, 0);

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

int tyuv2jpeg(unsigned char* yuv_buffer, int yuv_size, int width, int height, int subsample, unsigned char** jpeg_buffer, unsigned long* jpeg_size, int quality) {
    tjhandle handle = NULL;
    int flags = 0;
    int padding = 1; // 1或4均可，但不能是0
    int need_size = 0;
    int ret = 0;

    handle = tjInitCompress();

    flags |= 0;

    need_size = tjBufSizeYUV2(width, padding, height, subsample);
    ALOGD("tyuv2jpeg: need_size = %d, yuv_size = %d", need_size, yuv_size);
    if (need_size != yuv_size) {
        printf("we detect yuv size: %d, but you give: %d, check again.\n", need_size, yuv_size);
        return 0;
    }

    ret = tjCompressFromYUV(handle, yuv_buffer, width, padding, height, subsample, jpeg_buffer, jpeg_size, quality, flags);
    if (ret < 0) {
        printf("compress to jpeg failed: %s\n", tjGetErrorStr());
    }

    tjDestroy(handle);

    return ret;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_myapplication_JNILoader_yuv2jpeg(JNIEnv *env, jobject thiz, jbyteArray buffer, jint h, jint w, jint yuv_size, jint quality) {
    ALOGD("yuv2jpeg: size = %d x %d, quality = %d", w, h, quality);

    jbyte *yuv_buffer = env->GetByteArrayElements(buffer, JNI_FALSE);
    unsigned char *jpegBuf;
    unsigned long jpegSize = 0;
    tyuv2jpeg((unsigned char *) yuv_buffer, yuv_size, w, h, TJSAMP_420, &jpegBuf, &jpegSize, quality);

    ALOGD("yuv2jpeg: jpegSize = %ld, jpegBuf = %u", jpegSize, sizeof(jpegBuf));

    jbyteArray array = env->NewByteArray(jpegSize);
    env->SetByteArrayRegion(array, 0, jpegSize, (jbyte *) jpegBuf);

    free(jpegBuf);
    env->ReleaseByteArrayElements(buffer, yuv_buffer, JNI_OK);
    return array;
}

#include <android/hardware_buffer_jni.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myapplication_JNILoader_processHardwareBuffer(JNIEnv *env, jclass clazz, jobject buffer) {
    EglHelper eglHelper = *new EglHelper();
    eglHelper.initEgl();
    eglHelper.makeCurrent();

    AHardwareBuffer *hardwareBuffer = AHardwareBuffer_fromHardwareBuffer(env, buffer);
    AHardwareBuffer_Desc desc;
    AHardwareBuffer_describe(hardwareBuffer, &desc);
    EGLClientBuffer clientBuf = eglGetNativeClientBufferANDROID(hardwareBuffer);
    EGLDisplay disp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    EGLint eglImageAttributes[] = {EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE};
    EGLImageKHR imageEGL = eglCreateImageKHR(disp, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, clientBuf, eglImageAttributes);
    ALOGD("processHardwareBuffer : width height %d %d\n", desc.width, desc.height);

    unsigned int fbo;
    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    //https://learnopengl-cn.readthedocs.io/zh/latest/04%20Advanced%20OpenGL/05%20Framebuffers/
    unsigned int textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
    //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, NULL);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glEGLImageTargetTexture2DOES(GL_TEXTURE_EXTERNAL_OES, imageEGL);
    glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, textureId, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    YUVRender yuvRender = *new YUVRender();
    yuvRender.onSurfaceCreated();
    yuvRender.onSurfaceChanged(desc.width, desc.height);
    yuvRender.onDrawFrame(textureId);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glFinish();

    eglHelper.breakCurrent();

    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_JNILoader_processPasteHardwareBuffer(JNIEnv* env, jclass clazz, jobject bitmap, jobject buffer) {
    EglHelper eglHelper = *new EglHelper();
    eglHelper.initEgl();
    eglHelper.makeCurrent();

    unsigned char *pixels;
    AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels));

    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);

    GLuint pasteTextureId;
    glGenTextures(1, &pasteTextureId);
    glBindTexture(GL_TEXTURE_2D, pasteTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, info.width, info.height, 0, GL_RGBA,GL_UNSIGNED_BYTE, pixels);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, 0);
    ALOGD("setBitmap : width height %d %d %d %d\n", pasteTextureId, info.width, info.height, info.flags);

    AndroidBitmap_unlockPixels(env, bitmap);


    AHardwareBuffer *hardwareBuffer = AHardwareBuffer_fromHardwareBuffer(env, buffer);
    AHardwareBuffer_Desc desc;
    AHardwareBuffer_describe(hardwareBuffer, &desc);
    EGLClientBuffer clientBuf = eglGetNativeClientBufferANDROID(hardwareBuffer);
    EGLDisplay disp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    EGLint eglImageAttributes[] = {EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE};
    EGLImageKHR imageEGL = eglCreateImageKHR(disp, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, clientBuf, eglImageAttributes);
    ALOGD("processHardwareBuffer : width height %d %d\n", desc.width, desc.height);

    unsigned int fbo;
    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    unsigned int textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
    glEGLImageTargetTexture2DOES(GL_TEXTURE_EXTERNAL_OES, imageEGL);
    glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, textureId, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    YUVPasteRender yuvPasteRender = *new YUVPasteRender();
    yuvPasteRender.onSurfaceCreated();
    yuvPasteRender.onSurfaceChanged(desc.width, desc.height);
    yuvPasteRender.onDrawFrame(textureId, pasteTextureId);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glFinish();

    eglHelper.breakCurrent();

    return 0;
}