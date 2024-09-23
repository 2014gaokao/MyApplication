package com.example.myapplication

import android.graphics.Bitmap

class JNILoader {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun stringFromJNI(copy : Bitmap): String
}