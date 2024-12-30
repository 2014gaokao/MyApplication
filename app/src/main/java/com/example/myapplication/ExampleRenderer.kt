package com.example.myapplication

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.media.ImageReader
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import android.util.Size
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors

class ExampleRenderer(private val context: Context) {
    private var programId = 0
    private val mOpenCameraManager = OpenCameraManager(context)
    private var mSize = Size(0, 0)
    private val mMVPMatrix = FloatArray(16)

    private var mPreviewTex : Int = 0
    private val mTexMatrix = FloatArray(16)
    private val mSurfaceTexture: SurfaceTexture by lazy {
        val st = SurfaceTexture(0)
        st.setDefaultBufferSize(1920, 1080)
        st
    }
    private val mPreviewSurface = Surface(mSurfaceTexture)

    private var mPreviewTex1 : Int = 0
    private val mTexMatrix1 = FloatArray(16)
    private val mSurfaceTexture1: SurfaceTexture by lazy {
        val st = SurfaceTexture(0)
        st.setDefaultBufferSize(1920, 1080)
        st
    }
    private val mPreviewSurface1 = Surface(mSurfaceTexture1)

    private val mEglCore by lazy {
        EglCore()
    }

    private val mInnerEglCore by lazy {
        EglCore(mEglCore.mEglContext)
    }

    private val mScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private val mInnerScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private val mImageReader = ImageReader.newInstance(1920, 1080, PixelFormat.RGBA_8888, 2)

    init {
        mScope.launch {
            mEglCore.initGL()
        }
        mInnerScope.launch {
            mInnerEglCore.initGL()
        }
        CoroutineScope(Dispatchers.Main).launch {
            mImageReader.setOnImageAvailableListener({
                val image = it.acquireLatestImage()
                if (image != null) {
                    Log.d("zcc", "onImageAvailable: ${image.width}, ${image.height}")
                    image.close()
                }
            }, null)
        }
    }


    private val coordBuffer by lazy {
        val array = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )
        val coodBuffer = ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES)
        val floatBuffer = coodBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer()
        floatBuffer.put(array)
        floatBuffer.position(0)
    }

    private val vertexBuffer by lazy {
        val array = floatArrayOf(
            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 0.0f, 1.0f,
        )
        val coodBuffer = ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES)
        val floatBuffer = coodBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer()
        floatBuffer.put(array)
        floatBuffer.position(0)
    }

    fun onSurfaceCreated() {
        mScope.launch {
            programId = OpenGLUtil.createProgram(context, "vert.glsl", "frag.glsl")

            mPreviewTex = OpenGLUtil.genOesTex()
            mSurfaceTexture.detachFromGLContext()
            mSurfaceTexture.attachToGLContext(mPreviewTex)
            mSurfaceTexture.setOnFrameAvailableListener({
                mScope.launch {
                    mSurfaceTexture.updateTexImage()
                    mSurfaceTexture.getTransformMatrix(mTexMatrix)
                    cropTex(mTexMatrix)
                    onDrawFrame()
                    mEglCore.swapBuffers()
                }

                mInnerScope.launch {
                    mInnerEglCore.updateSurface(mImageReader.surface, Size(mImageReader.width, mImageReader.height))
                    onDrawFrame()
                    mInnerEglCore.swapBuffers()
                }
            }, null)

            mPreviewTex1 = OpenGLUtil.genOesTex()
            mSurfaceTexture1.detachFromGLContext()
            mSurfaceTexture1.attachToGLContext(mPreviewTex1)
            mSurfaceTexture1.setOnFrameAvailableListener({
                mScope.launch {
                    mSurfaceTexture1.updateTexImage()
                    mSurfaceTexture1.getTransformMatrix(mTexMatrix1)
                    cropTex(mTexMatrix1)
                    onDrawFrame()
                    mEglCore.swapBuffers()
                }

                mInnerScope.launch {
                    mInnerEglCore.updateSurface(mImageReader.surface, Size(mImageReader.width, mImageReader.height))
                    onDrawFrame()
                    mInnerEglCore.swapBuffers()
                }
            }, null)

            mOpenCameraManager.openCamera(mPreviewSurface, "0", mPreviewSurface1, "1")
        }
    }

    fun onSurfaceChanged(surface: Surface, width: Int, height: Int) {
        mScope.launch {
            mEglCore.updateSurface(surface, Size(width, height))
            mSize = Size(width, height)
            GLES30.glViewport(0, 0, width, height)

            Matrix.setIdentityM(mMVPMatrix, 0)
            Matrix.scaleM(mMVPMatrix, 0, 1f, -1f, 1f)
        }
    }

    private fun cropTex(matrix: FloatArray) {
        Matrix.translateM(matrix, 0, 0.5f, 0.5f, 0f)
        Matrix.scaleM(matrix, 0, 1f, 0.5f, 0f)
        Matrix.translateM(matrix, 0, -0.5f, -0.5f, 0f)
    }

    private fun onDrawFrame() {
        //两句一起才能设置红色
        GLES30.glClearColor(1f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(programId)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)

        GLES30.glUniform1i(GLES30.glGetUniformLocation(programId, "vTexture"), 0)

        GLES30.glEnableVertexAttribArray(GLES30.glGetAttribLocation(programId, "aPosition"))
        GLES30.glVertexAttribPointer(GLES30.glGetAttribLocation(programId, "aPosition"), 4, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        GLES30.glEnableVertexAttribArray(GLES30.glGetAttribLocation(programId, "texCoord"))
        GLES30.glVertexAttribPointer(GLES30.glGetAttribLocation(programId, "texCoord"), 2, GLES30.GL_FLOAT, false, 0, coordBuffer);

        GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(programId, "uMVPMatrix"), 1, false, mMVPMatrix, 0)

        GLES30.glViewport(0, 0, mSize.width, mSize.height / 2)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mPreviewTex)
        GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(programId, "uTexMatrix"), 1, false, mTexMatrix, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glViewport(0, mSize.height / 2, mSize.width, mSize.height / 2)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mPreviewTex1)
        GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(programId, "uTexMatrix"), 1, false, mTexMatrix1, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun surfaceDestroyed() {
        runBlocking {
            mOpenCameraManager.close()
            mEglCore.release()
        }
    }
}