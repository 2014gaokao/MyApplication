package com.example.myapplication

import android.opengl.GLES30
import android.opengl.GLSurfaceView

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

    }

    override fun onDrawFrame(p0: GL10?) {
        //两句一起才能设置红色
        GLES30.glClearColor(1f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }
}