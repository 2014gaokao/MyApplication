#https://blog.csdn.net/qq_37565670/article/details/122587954

cmake_minimum_required(VERSION 3.10.2)

project("jnidemo")

set(CMAKE_VERBOSE_MAKEFILE on)
set(libs "${CMAKE_SOURCE_DIR}/libs")
set(cpps "${CMAKE_SOURCE_DIR}/src/main/cpp")
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

add_library(turbojpeg STATIC IMPORTED)
set_target_properties(turbojpeg PROPERTIES IMPORTED_LOCATION "${PROJECT_SOURCE_DIR}/../../../libs/${ANDROID_ABI}/libturbojpeg.a")

add_library(native-lib SHARED native-lib.cpp EglHelper.cpp GrayRender.cpp SplitRender.cpp YUVRender.cpp)

find_library(lib_log log)

target_link_libraries(native-lib
        android #AHardwareBuffer_allocate
        jnigraphics #AndroidBitmap_getInfo
        GLESv3 #glGenTextures
        EGL
        ${lib_log}
        turbojpeg)