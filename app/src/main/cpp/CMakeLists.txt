#https://blog.csdn.net/qq_37565670/article/details/122587954

cmake_minimum_required(VERSION 3.10.2)

project("jnidemo")

add_library(native-lib SHARED native-lib.cpp EglHelper.cpp GrayRender.cpp)

find_library(lib_log log)

target_link_libraries(native-lib
        android #AHardwareBuffer_allocate
        jnigraphics #AndroidBitmap_getInfo
        GLESv3 #glGenTextures
        EGL
        ${lib_log})