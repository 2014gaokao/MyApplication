package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.EGL14
import android.opengl.EGLDisplay
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OpenGLUtil {
    companion object {

        fun readFbo(w: Int, h: Int): Bitmap{
            val rgbaBuf = ByteBuffer.allocate(w * h * 4).order(ByteOrder.nativeOrder())
            GLES30.glFlush()
            GLES30.glReadPixels(0, 0, w, h, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, rgbaBuf)
            rgbaBuf.rewind()
            rgbaBuf.position(0)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(rgbaBuf)
            return bmp
        }

        fun checkGLError(op : String) {
            var error : Int
            while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
                Log.e("zcc", "$op : glError $error")
                throw RuntimeException()
            }
        }

        fun createProgram(context: Context, vertex : String, frag : String) : Int {
            val vertString = getShader(context, vertex)
            val fragString = getShader(context, frag)
            val vertId = compileShader(GLES30.GL_VERTEX_SHADER, vertString)
            val fragId = compileShader(GLES30.GL_FRAGMENT_SHADER, fragString)
            val programId = GLES30.glCreateProgram()
            GLES30.glAttachShader(programId, vertId)
            GLES30.glAttachShader(programId, fragId)
            GLES30.glLinkProgram(programId)
            GLES30.glDeleteShader(vertId)
            GLES30.glDeleteShader(fragId)
            checkGLError("init")
            return programId
        }

        fun loadJpegToTexture(context: Context, id : Int) : Int {
            val bitmap = BitmapFactory.decodeResource(context.resources, id)

            val textureId = IntArray(1)
            GLES30.glGenTextures(1, textureId, 0)

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0])
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            return textureId[0]
        }

        fun genOesTex() : Int {
            val textureId = IntArray(1)
            GLES30.glGenTextures(1, textureId, 0)
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0])
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
            return textureId[0]
        }

        fun compileShader(shaderType: Int, shader: String) : Int {
            val shaderId = GLES30.glCreateShader(shaderType)
            GLES30.glShaderSource(shaderId, shader)
            GLES30.glCompileShader(shaderId)
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                Log.e("zcc", "Error: ${GLES30.glGetShaderInfoLog(shaderId)}")
                GLES30.glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }

        fun getShader(context: Context?, file : String) : String {
            val needPrint = true
            context?.let {
                val shader = loadFileToString(it, file)
                if (shader.isEmpty()) {
                    throw NullPointerException()
                }
                if (needPrint) {
                    Log.d("zcc", shader)
                }
                return shader
            }?:run {
                throw NullPointerException()
            }
        }

        private fun loadFileToString(context : Context, fileName : String) : String {
            return context.assets.open(fileName).bufferedReader().use { it.readText() }
        }

        fun dumpEGLExtensions(eglDisplay: EGLDisplay) {
            val extension = EGL14.eglQueryString(eglDisplay, EGL14.EGL_EXTENSIONS)
            extension?.let {
                it.split(" ").forEach {
                    Log.d("zcc", "$it")
                }
            }
        }

    }
}