#include <jni.h>
#include <string>

#include <android/log.h>

#define LOG_TAG "zcc"

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_JNILoader_stringFromJNI(JNIEnv* env, jobject obj) {
    std::string hello = "Hello from C++";
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "nullptr");
    return env->NewStringUTF(hello.c_str());
}