package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OpenCameraManager(private val context: Context) {

    private val mCameraManager by lazy {
        context.getSystemService(CameraManager::class.java)
    }
    private var mCameraDevice: CameraDevice? = null
    private lateinit var mPreviewSurface : Surface
    private lateinit var mCameraSession : CameraCaptureSession
    private lateinit var mPreviewBuilder : CaptureRequest.Builder

    private var mCameraDevice1: CameraDevice? = null
    private lateinit var mPreviewSurface1 : Surface
    private lateinit var mCameraSession1 : CameraCaptureSession
    private lateinit var mPreviewBuilder1 : CaptureRequest.Builder

    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    private val cameraHandler = Handler(cameraThread.looper)

    fun openCamera(surface: Surface, id: String, surface1: Surface, id1: String) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            mCameraDevice = getCameraDevice(id)
            mCameraDevice1 = getCameraDevice(id1)

            mPreviewSurface = surface
            mPreviewSurface1 = surface1

            mCameraSession = startPreviewSession(mCameraDevice, mPreviewSurface)!!
            mCameraSession1 = startPreviewSession(mCameraDevice1, mPreviewSurface1)!!

            startPreview()
            startPreview1()
        }
    }

    fun close() {
        mCameraDevice?.close()
        mCameraDevice1?.close()
    }

    private fun startPreview() {
        mCameraDevice?.let {
            mPreviewBuilder = it.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewBuilder.addTarget(mPreviewSurface)
            mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
            }, cameraHandler)
        }
    }
    private fun startPreview1() {
        mCameraDevice1?.let {
            mPreviewBuilder1 = it.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewBuilder1.addTarget(mPreviewSurface1)
            mCameraSession1.setRepeatingRequest(mPreviewBuilder1.build(), object : CameraCaptureSession.CaptureCallback() {
            }, cameraHandler)
        }
    }

    private suspend fun startPreviewSession(device: CameraDevice?, surface: Surface): CameraCaptureSession? {
        return suspendCoroutine {
            val previewConfig = OutputConfiguration(surface)
            val config = SessionConfiguration(SessionConfiguration.SESSION_REGULAR, mutableListOf(previewConfig),
                HandleExecutor(cameraHandler), object : CameraCaptureSession.StateCallback () {
                    override fun onConfigured(session: CameraCaptureSession) {
                        it.resume(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        it.resumeWithException(RuntimeException("onConfigureFailed"))
                    }
                })
            device?.createCaptureSession(config)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCameraDevice(id: String) : CameraDevice {
        return suspendCoroutine {
            mCameraManager?.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    it.resume(camera)
                }

                override fun onDisconnected(p0: CameraDevice) {
                    //TODO("Not yet implemented")
                }

                override fun onError(p0: CameraDevice, p1: Int) {
                    //TODO("Not yet implemented")
                }

            }, cameraHandler)
        }
    }

    class HandleExecutor(private val cameraHandler : Handler) : Executor {
        override fun execute(p0: Runnable?) {
            p0?.let { cameraHandler.post(it) }
        }
    }

}