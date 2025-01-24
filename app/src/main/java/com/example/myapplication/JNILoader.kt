package com.example.myapplication

import android.graphics.Bitmap

class JNILoader {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun stringFromJNI(copy : Bitmap): String

    external fun yuv2jpeg(byteArray: ByteArray, height: Int, width: Int, yuvSize: Int, quality: Int): ByteArray
}