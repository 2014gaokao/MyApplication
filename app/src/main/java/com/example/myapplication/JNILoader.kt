package com.example.myapplication

import android.graphics.Bitmap
import android.hardware.HardwareBuffer

class JNILoader {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun stringFromJNI(copy : Bitmap): String

    external fun yuv2jpeg(byteArray: ByteArray, height: Int, width: Int, yuvSize: Int, quality: Int): ByteArray

    external fun processHardwareBuffer(hardwareBuffer: HardwareBuffer?): Int

    external fun processPasteHardwareBuffer(copy : Bitmap, hardwareBuffer: HardwareBuffer?): Int
}