package com.example.myapplication

class JNILoader {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun stringFromJNI(): String
}