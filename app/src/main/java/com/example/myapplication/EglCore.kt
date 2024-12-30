package com.example.myapplication

import android.opengl.EGL14
import android.opengl.EGL15
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLSurface
import android.util.Size
import android.view.Surface

class EglCore(private val mShareContext : EGLContext = EGL14.EGL_NO_CONTEXT) {

    private var mEglDisplay = EGL14.EGL_NO_DISPLAY
    var mEglContext: EGLContext = EGL14.EGL_NO_CONTEXT
        private set(value) {field = value}
    private var mEGLConfig: EGLConfig? = null
    private var mEGLSurface = EGL14.EGL_NO_SURFACE

    fun initGL() {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed")
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw RuntimeException("eglInitialize failed")
        }

        val attribList = intArrayOf (
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 8,
            EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL15.EGL_OPENGL_ES3_BIT,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(mEglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)) {
            throw RuntimeException("eglChooseConfig failed")
        }

        mEGLConfig = configs[0] ?: throw RuntimeException("eglChooseConfig failed")

        val contextAttribs = intArrayOf (
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEGLConfig, mShareContext, contextAttribs, 0)
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw RuntimeException("eglCreateContext failed")
        }

        mEGLSurface = createPbufferSurface()

        EGL14.eglMakeCurrent(mEglDisplay, mEGLSurface, mEGLSurface, mEglContext)
    }

    fun updateSurface(surface: Surface, size: Size) {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(mEglDisplay, mEGLSurface)
        }
        mEGLSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEGLConfig, surface, null, 0)
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("eglCreateWindowSurface failed")
        }
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEGLSurface, mEGLSurface, mEglContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }

    fun release() {
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(mEglDisplay, mEGLSurface)
        EGL14.eglDestroyContext(mEglDisplay, mEglContext)
        EGL14.eglTerminate(mEglDisplay)
    }

    private fun createPbufferSurface() : EGLSurface {
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, 1,
            EGL14.EGL_HEIGHT, 1,
            EGL14.EGL_NONE
        )
        return EGL14.eglCreatePbufferSurface(mEglDisplay, mEGLConfig, surfaceAttribs, 0)
    }

    fun swapBuffers() {
        EGL14.eglSwapBuffers(mEglDisplay, mEGLSurface)
    }

}